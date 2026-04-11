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
      isThinking: false,
      hasCompletedSetup: true,
    });
    vi.clearAllMocks();
  });

  it('queues a request when no runtime is online', async () => {
    const store = useContinuumStore.getState();
    await store.requestAI('agent-1', 'tpl-1', { text: 'hello' });
    
    const newState = useContinuumStore.getState();
    expect(newState.aiRequestQueue.length).toBe(1);
    expect(newState.aiRequestQueue[0]?.status).toBe('pending');
    expect(newState.isThinking).toBe(false);
  });
});
