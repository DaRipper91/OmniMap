# Feature Implementation Plan: feat_continuum_task_9_5

## 🔍 Analysis & Context
*   **Objective:** Implement Error/Offline recovery for AI requests (Task 9.5). If no AI runtime is available or a network error occurs, queue the request and automatically retry when a runtime comes back online.
*   **Affected Files:**
    *   `src/types.ts`
    *   `src/store.ts`
    *   `package.json`
*   **Key Dependencies:** `zustand`, `ai-bridge.ts`.
*   **Risks/Edge Cases:**
    *   `store.ts` imports Capacitor native modules (`@capacitor/filesystem`, `@capacitor/core`), which will crash in a Node test environment unless properly mocked.
    *   Avoid duplicate requests if `retryQueuedRequests` is called concurrently.
    *   State must not get stuck with `isThinking: true` if an offline queueing event happens.
    *   The `aiRequestQueue` must persist across app restarts, so the Zustand `partialize` filter needs updating.

## 📋 Micro-Step Checklist
- [ ] Phase 1: Test Infrastructure Setup
  - [ ] Step 1.A: Install `vitest`.
  - [ ] Step 1.B: Write `src/store.test.ts` with mocks for Capacitor and `ai-bridge`.
- [ ] Phase 2: State Definition Expansion
  - [ ] Step 2.A: Define `QueuedAIRequest` in `src/types.ts`.
  - [ ] Step 2.B: Add queue fields to `ContinuumState` and `ContinuumActions` in `src/store.ts`.
- [ ] Phase 3: Queue & Retry Implementation
  - [ ] Step 3.A: Modify `requestAI` to queue offline/failed requests.
  - [ ] Step 3.B: Implement `retryQueuedRequests` and `removeQueuedRequest`.
  - [ ] Step 3.C: Modify `checkModelHealth` to trigger retry.
  - [ ] Step 3.D: Add `aiRequestQueue` to `partialize` for persistence.
  - [ ] Step 3.E: Verify via Unit Tests.
- [ ] Phase 4: Documentation
  - [ ] Step 4.A: Update `PROJECT_REPORT.md` and `PLAN.md`.

## 📝 Step-by-Step Implementation Details

### Phase 1: Test Infrastructure Setup
1.  **Step 1.A (Install Runner):**
    *   *Action:* Run `npm install -D vitest`.
    *   *Action:* Update `package.json` scripts to include `"test": "vitest run"`.

2.  **Step 1.B (The Unit Test Harness):** Define the verification requirement.
    *   *Target File:* `src/store.test.ts`
    *   *Exact Change:* Create a test file that mocks dependencies and tests the Zustand store directly.
    ```typescript
    import { describe, it, expect, vi, beforeEach } from 'vitest';
    import { useContinuumStore } from './store';

    // Mock Capacitor
    vi.mock('@capacitor/core', () => ({ Capacitor: { getPlatform: () => 'web' } }));
    vi.mock('@capacitor/filesystem', () => ({ Filesystem: {}, Directory: {}, Encoding: {} }));
    
    // Mock AI Bridge
    vi.mock('./ai-bridge', () => ({ callLocalLLM: vi.fn() }));

    describe('ContinuumStore AI Queue', () => {
      beforeEach(() => {
        useContinuumStore.setState({
          runtimes: [],
          agents: [{ id: 'agent-1', name: 'Test Agent', persona: '', role: '', skillIds: [], status: 'active' }],
          promptTemplates: [{ id: 'tpl-1', name: 'Test Tpl', systemPrompt: 'sys', userPromptFormat: 'user {{content}}' }],
          aiRequestQueue: [],
          isThinking: false
        });
        vi.clearAllMocks();
      });

      it('queues a request when no runtime is online', async () => {
        const store = useContinuumStore.getState();
        await store.requestAI('agent-1', 'tpl-1', { text: 'hello' });
        
        const newState = useContinuumStore.getState();
        expect(newState.aiRequestQueue.length).toBe(1);
        expect(newState.aiRequestQueue[0].status).toBe('pending');
        expect(newState.isThinking).toBe(false);
      });
    });
    ```

### Phase 2: State Definition Expansion
1.  **Step 2.A (Define Types):** Define the queue types.
    *   *Target File:* `src/types.ts`
    *   *Exact Change:* Export a new interface `QueuedAIRequest` and update `ContinuumState`.
    ```typescript
    export interface QueuedAIRequest {
      id: string;
      agentId: string;
      templateId: string;
      context: any;
      status: 'pending' | 'failed';
      timestamp: number;
      retryCount: number;
    }
    ```
    *   *Exact Change:* Add `aiRequestQueue: QueuedAIRequest[];` to `ContinuumState`.

2.  **Step 2.B (Add Action Signatures):**
    *   *Target File:* `src/store.ts`
    *   *Exact Change:* Update `interface ContinuumActions` with:
    ```typescript
      retryQueuedRequests: () => Promise<void>;
      removeQueuedRequest: (id: string) => void;
    ```
    *   *Exact Change:* Update the `initialState` object to include `aiRequestQueue: []`.

### Phase 3: Queue & Retry Implementation
1.  **Step 3.A (Offline Intercept):** Intercept offline requests in `requestAI`.
    *   *Target File:* `src/store.ts`
    *   *Exact Change:* Update `requestAI` to enqueue failures.
    ```typescript
      requestAI: async (agentId: string, templateId: string, context: any) => {
        set({ isThinking: true });
        const state = get();
        const agent = state.agents.find(a => a.id === agentId);
        const template = state.promptTemplates.find(t => t.id === templateId);
        const runtime = state.runtimes.find(r => r.status === 'online');

        if (!agent || !template) {
          console.error('AI Request failed: Missing agent or template.');
          set({ isThinking: false });
          return;
        }

        const queueRequest = () => {
          const id = crypto.randomUUID ? crypto.randomUUID() : Date.now().toString();
          set((s: ContinuumState) => ({
            aiRequestQueue: [...s.aiRequestQueue, {
              id,
              agentId,
              templateId,
              context,
              status: 'pending',
              timestamp: Date.now(),
              retryCount: 0
            }]
          }));
        };

        if (!runtime) {
          console.warn('No online runtime found. Queuing AI request.');
          queueRequest();
          set({ isThinking: false });
          return;
        }

        try {
          const userPrompt = template.userPromptFormat.replace('{{content}}', JSON.stringify(context));
          const rawResponse = await callLocalLLM(runtime, template.systemPrompt, userPrompt);
          
          const jsonBlockRegex = /```json\s*([\s\S]*?)\s*```|({[\s\S]*})/;
          const match = rawResponse.match(jsonBlockRegex);
          const jsonString = match ? (match[1] || match[2]) : null;

          if (jsonString) {
            const mutationPayload = JSON.parse(jsonString);
            get().proposeMutation({
              type: mutationPayload.type || 'CREATE',
              entity: mutationPayload.entity || 'TASK',
              payload: mutationPayload.payload,
              agentId: agent.id
            });
          }
        } catch (error) {
          console.error('Inference error:', error);
          console.warn('Queuing AI request for retry.');
          queueRequest();
        } finally {
          set({ isThinking: false });
        }
      },
    ```

2.  **Step 3.B (Queue Processors):** Implement the queue consumers.
    *   *Target File:* `src/store.ts`
    *   *Exact Change:* Add `retryQueuedRequests` and `removeQueuedRequest` implementations.
    ```typescript
      removeQueuedRequest: (id: string) => set((state: ContinuumState) => ({
        aiRequestQueue: state.aiRequestQueue.filter(r => r.id !== id)
      })),

      retryQueuedRequests: async () => {
        const state = get();
        const queue = [...state.aiRequestQueue];
        if (queue.length === 0) return;

        const runtime = state.runtimes.find(r => r.status === 'online');
        if (!runtime) return;

        for (const req of queue) {
          get().removeQueuedRequest(req.id);
          // Re-attempt request immediately
          await get().requestAI(req.agentId, req.templateId, req.context);
        }
      },
    ```

3.  **Step 3.C (Auto-Retry Wiring):**
    *   *Target File:* `src/store.ts`
    *   *Exact Change:* Modify `checkModelHealth` to check if the status transitioned from offline to online.
    ```typescript
      checkModelHealth: async (id: string) => {
        const state = get();
        const runtime = state.runtimes.find(r => r.id === id);
        if (!runtime) return false;

        try {
          const controller = new AbortController();
          const timeoutId = setTimeout(() => controller.abort(), 2000);
          const response = await fetch(`${runtime.baseUrl}/v1/models`, { signal: controller.signal });
          clearTimeout(timeoutId);

          const isOnline = response.ok;
          const wasOffline = runtime.status !== 'online';
          
          set((state: ContinuumState) => ({
            runtimes: state.runtimes.map(r => r.id === id ? { ...r, status: isOnline ? 'online' : 'offline', lastChecked: Date.now() } : r)
          }));

          if (isOnline && wasOffline) {
            get().retryQueuedRequests();
          }

          return isOnline;
        } catch {
          // ... retain existing catch block which sets it offline
    ```
    *   *Exact Change:* Ensure the `catch` block in `checkModelHealth` also handles the state properly (it already sets it to offline, verify it is retained).

4.  **Step 3.D (Persistence Update):**
    *   *Target File:* `src/store.ts`
    *   *Exact Change:* Add `aiRequestQueue: state.aiRequestQueue,` to the object returned by the `partialize` function (around line 607) to ensure queued requests survive app restarts.

5.  **Step 3.E (Verify):**
    *   *Action:* Run `npm run test src/store.test.ts`.
    *   *Success:* Test passes and queue logic is confirmed robust.

### Phase 4: Documentation Update
1.  **Step 4.A:** Mark task complete.
    *   *Target Files:* `PLAN.md`, `PROJECT_REPORT.md`
    *   *Exact Change:* Check off `Task 9.5: Implement Error/Offline recovery for AI requests.` Update `PROJECT_REPORT.md` with details about the new `aiRequestQueue` architecture in the Zustand store and the integration of `vitest` for the safety harness.

## 🧪 Global Testing Strategy
*   **Unit Tests:** Verify `requestAI` logic queues correctly and drops `isThinking` flag reliably without an online connection. Use Vitest to mock native capacitor layers.
*   **Integration Tests (Manual):** Disconnect network/local LLM, trigger AI mutation, confirm UI doesn't hang, start local LLM, observe auto-recovery.

## 🎯 Success Criteria
*   When a request is made with no online runtime, it is added to the `aiRequestQueue`.
*   When an online runtime comes back and `checkModelHealth` detects it, it processes `retryQueuedRequests`.
*   Failed API requests automatically transition to the pending queue without crashing the app.
*   Pending requests persist through an app restart.
