import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { Filesystem, Directory, Encoding } from '@capacitor/filesystem';
import { Capacitor } from '@capacitor/core';
import { SuggestedActionStatus } from './types';
import { callLocalLLM } from './ai-bridge';
import { checkRuntime } from './ai-bridge';
const initialState = {
    nodes: [],
    edges: [],
    layout: { positions: {}, zoom: 1, pan: { x: 0, y: 0 } },
    runtimes: [
        {
            id: 'gpt4all-local',
            name: 'GPT4All (Local)',
            baseUrl: 'http://100.115.141.124:4891',
            status: 'unknown',
            lastChecked: 0
        }
    ],
    promptTemplates: [
        {
            id: 'task-generator',
            name: 'Task Generator',
            systemPrompt: 'You are the Architect Agent. Your goal is to analyze the user input and generate actionable tasks in JSON format. Response must be a single JSON block.',
            userPromptFormat: 'Analyze this context and create tasks: {{content}}'
        }
    ],
    downloadProgress: {},
    projects: [],
    tasks: [],
    notes: [],
    suggestedActions: [],
    models: [],
    agents: [
        {
            id: 'jules',
            name: 'Jules',
            persona: 'Strategic Architect',
            role: 'System Design & Task Decomposition',
            skillIds: ['internal-task-gen'],
            status: 'active'
        }
    ],
    skills: [
        {
            id: 'internal-task-gen',
            name: 'Task Generation',
            description: 'Ability to create new task nodes in the graph.',
            type: 'internal',
            permissions: ['WRITE_TASK']
        }
    ],
    pendingMutations: [],
    undoHistory: [],
    activeProjectId: null,
    activeModelId: null,
    lastSyncTimestamp: 0,
    isScanning: false,
    isThinking: false,
    aiStatus: 'idle',
    aiError: null,
    hasCompletedSetup: false,
    portals: [],
    aiRequestQueue: [],
};
// Custom Capacitor Storage Engine
const capacitorStorage = {
    getItem: async (name) => {
        try {
            const platform = Capacitor.getPlatform();
            if (platform === 'web')
                return localStorage.getItem(name);
            const result = await Filesystem.readFile({
                path: `${name}.json`,
                directory: Directory.Documents,
                encoding: Encoding.UTF8
            });
            return result.data;
        }
        catch {
            return null;
        }
    },
    setItem: async (name, value) => {
        const platform = Capacitor.getPlatform();
        if (platform === 'web') {
            localStorage.setItem(name, value);
            return;
        }
        await Filesystem.writeFile({
            path: `${name}.json`,
            data: value,
            directory: Directory.Documents,
            encoding: Encoding.UTF8
        });
    },
    removeItem: async (name) => {
        const platform = Capacitor.getPlatform();
        if (platform === 'web') {
            localStorage.removeItem(name);
            return;
        }
        await Filesystem.deleteFile({
            path: `${name}.json`,
            directory: Directory.Documents
        });
    }
};
export const useContinuumStore = create()(persist((set, get) => ({
    ...initialState,
    addNode: (node) => set((state) => ({
        nodes: [...state.nodes, node],
    })),
    updateNode: (id, updates) => set((state) => ({
        nodes: state.nodes.map((n) => n.id === id ? { ...n, ...updates, updatedAt: Date.now() } : n),
    })),
    deleteNode: (id) => set((state) => ({
        nodes: state.nodes.filter((n) => n.id !== id),
        edges: state.edges.filter((e) => e.sourceId !== id && e.targetId !== id),
    })),
    addEdge: (edge) => set((state) => ({
        edges: [...state.edges, edge],
    })),
    removeEdge: (id) => set((state) => ({
        edges: state.edges.filter((e) => e.id !== id),
    })),
    migrateToGraph: () => {
        const { projects, tasks, notes } = get();
        const newNodes = [];
        const newEdges = [];
        projects.forEach(p => {
            newNodes.push({
                id: p.id, type: 'PROJECT', title: p.name, description: p.description,
                data: { path: p.path, tags: p.tags }, metadata: {}, createdAt: p.createdAt, updatedAt: p.updatedAt
            });
        });
        tasks.forEach(t => {
            newNodes.push({
                id: t.id, type: 'TASK', title: t.title, description: t.description,
                data: { status: t.status, priority: t.priority, tags: t.tags }, metadata: {},
                createdAt: t.createdAt, updatedAt: t.updatedAt
            });
            newEdges.push({ id: `edge-${t.projectId}-${t.id}`, sourceId: t.projectId, targetId: t.id, type: 'PARENT_OF', metadata: {}, createdAt: Date.now() });
        });
        notes.forEach(n => {
            if (!newNodes.find(node => node.id === n.id)) {
                newNodes.push({
                    id: n.id, type: 'IDEA', title: 'Ideal Note', description: n.content.substring(0, 50) + '...',
                    data: { content: n.content, tags: n.tags }, metadata: {}, createdAt: n.createdAt, updatedAt: n.updatedAt
                });
            }
            if (n.projectId && !newEdges.find(e => e.sourceId === n.projectId && e.targetId === n.id)) {
                newEdges.push({ id: `edge-${n.projectId}-${n.id}`, sourceId: n.projectId, targetId: n.id, type: 'PARENT_OF', metadata: {}, createdAt: Date.now() });
            }
        });
        set({ nodes: newNodes, edges: newEdges });
    },
    registerRuntime: (runtime) => set((state) => ({
        runtimes: [...state.runtimes, runtime],
    })),
    updateRuntime: (id, updates) => set((state) => ({
        runtimes: state.runtimes.map((r) => r.id === id ? { ...r, ...updates } : r),
    })),
    updateRuntimeStatus: (id, status) => set((state) => ({
        runtimes: state.runtimes.map((r) => r.id === id ? { ...r, status, lastChecked: Date.now() } : r),
    })),
    addPromptTemplate: (template) => set((state) => ({
        promptTemplates: [...state.promptTemplates, template],
    })),
    updateDownloadProgress: (modelId, progress) => set((state) => ({
        downloadProgress: { ...state.downloadProgress, [modelId]: progress },
    })),
    requestAI: async (agentId, templateId, context, retries = 0) => {
        set({ isThinking: true, aiStatus: retries > 0 ? 'retrying' : 'thinking', aiError: null });
        const state = get();
        const agent = state.agents.find(a => a.id === agentId);
        const template = state.promptTemplates.find(t => t.id === templateId);
        const runtime = state.runtimes.find(r => r.status === 'online');
        if (!agent || !template) {
            set({ isThinking: false, aiStatus: 'error', aiError: 'Missing agent or prompt template.' });
            return;
        }
        if (!runtime) {
            set({ isThinking: false, aiStatus: 'error', aiError: 'No online runtime available.' });
            return;
        }
        const isOnline = await checkRuntime(runtime);
        if (!isOnline) {
            get().updateRuntimeStatus(runtime.id, 'offline');
            set({ isThinking: false, aiStatus: 'error', aiError: 'Runtime unreachable. Marking as offline.' });
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
                set({ isThinking: false, aiStatus: 'idle', aiError: null });
            }
            else {
                throw new Error("Invalid response format. No JSON payload found.");
            }
        }
        catch (error) {
            console.error('Inference error:', error);
            if (retries < 2) {
                const backoffDelay = Math.pow(2, retries) * 1000;
                setTimeout(() => {
                    get().requestAI(agentId, templateId, context, retries + 1);
                }, backoffDelay);
            }
            else {
                set({
                    isThinking: false,
                    aiStatus: 'error',
                    aiError: `AI Request failed: ${error.message || 'Unknown error'}`
                });
            }
        }
    },
    removeQueuedRequest: (id) => set((state) => ({
        aiRequestQueue: state.aiRequestQueue.filter(r => r.id !== id)
    })),
    retryQueuedRequests: async () => {
        const state = get();
        const queue = [...state.aiRequestQueue];
        if (queue.length === 0)
            return;
        const runtime = state.runtimes.find(r => r.status === 'online');
        if (!runtime)
            return;
        for (const req of queue) {
            get().removeQueuedRequest(req.id);
            await get().requestAI(req.agentId, req.templateId, req.context);
        }
    },
    downloadModel: async (modelId) => {
        const model = get().models.find(m => m.id === modelId);
        if (!model || !model.downloadUrl)
            return;
        let progress = 0;
        const interval = setInterval(() => {
            progress += 10;
            get().updateDownloadProgress(modelId, progress);
            if (progress >= 100) {
                clearInterval(interval);
                set((state) => ({
                    models: state.models.map(m => m.id === modelId ? { ...m, status: 'available' } : m)
                }));
            }
        }, 500);
    },
    updateNodePosition: (nodeId, x, y) => set((state) => ({
        layout: {
            ...state.layout,
            positions: {
                ...state.layout.positions,
                [nodeId]: { x, y }
            }
        }
    })),
    updateViewport: (zoom, pan) => set((state) => ({
        layout: { ...state.layout, zoom, pan }
    })),
    addProject: (project) => set((state) => ({
        projects: [...state.projects, project],
    })),
    updateProject: (id, updates) => set((state) => ({
        projects: state.projects.map((p) => p.id === id ? { ...p, ...updates, updatedAt: Date.now() } : p),
    })),
    setActiveProject: (id) => set({ activeProjectId: id }),
    addTask: (task) => set((state) => ({
        tasks: [...state.tasks, task],
    })),
    updateTask: (id, updates) => set((state) => ({
        tasks: state.tasks.map((t) => t.id === id ? { ...t, ...updates, updatedAt: Date.now() } : t),
    })),
    deleteTask: (id) => set((state) => ({
        tasks: state.tasks.filter((t) => t.id !== id),
    })),
    addNote: (note) => set((state) => ({
        notes: [...state.notes, note],
    })),
    updateNote: (id, updates) => set((state) => ({
        notes: state.notes.map((n) => n.id === id ? { ...n, ...updates, updatedAt: Date.now() } : n),
    })),
    deleteNote: (id) => set((state) => ({
        notes: state.notes.filter((n) => n.id !== id),
    })),
    addSuggestedAction: (action) => set((state) => ({
        suggestedActions: [...state.suggestedActions, action],
    })),
    updateSuggestedAction: (id, updates) => set((state) => ({
        suggestedActions: state.suggestedActions.map((a) => a.id === id ? { ...a, ...updates, updatedAt: Date.now() } : a),
    })),
    snoozeAction: (id, durationMinutes) => set((state) => ({
        suggestedActions: state.suggestedActions.map((a) => a.id === id ? {
            ...a,
            status: SuggestedActionStatus.SNOOZED,
            snoozedUntil: durationMinutes ? Date.now() + (durationMinutes * 60 * 1000) : undefined,
            updatedAt: Date.now()
        } : a),
    })),
    archiveAction: (id) => set((state) => ({
        suggestedActions: state.suggestedActions.map((a) => a.id === id ? {
            ...a,
            status: SuggestedActionStatus.ARCHIVED,
            snoozedUntil: undefined,
            updatedAt: Date.now()
        } : a),
    })),
    acceptAction: (id) => {
        const action = get().suggestedActions.find((a) => a.id === id);
        if (!action || !action.metadata)
            return;
        set((state) => ({
            suggestedActions: state.suggestedActions.map((a) => a.id === id ? { ...a, status: SuggestedActionStatus.ACCEPTED, snoozedUntil: undefined, updatedAt: Date.now() } : a),
        }));
        const { actionType, metadata } = action;
        let entity = 'TASK';
        let type = 'CREATE';
        if (actionType === 'update_task' || actionType === 'refactor')
            type = 'UPDATE';
        if (actionType !== 'other') {
            get().proposeMutation({ type, entity, payload: metadata, agentId: 'continuum-suggester' });
        }
    },
    deleteSuggestedAction: (id) => set((state) => ({
        suggestedActions: state.suggestedActions.filter((a) => a.id !== id),
    })),
    resurfaceActions: () => {
        const now = Date.now();
        set((state) => ({
            suggestedActions: state.suggestedActions.map((a) => {
                if (a.status === SuggestedActionStatus.SNOOZED && a.snoozedUntil && a.snoozedUntil <= now) {
                    return { ...a, status: SuggestedActionStatus.ACTIVE, snoozedUntil: undefined, updatedAt: now };
                }
                return a;
            })
        }));
    },
    registerModel: (model) => set((state) => ({
        models: [...state.models, model],
    })),
    updateModelStatus: (id, status) => set((state) => ({
        models: state.models.map((m) => m.id === id ? { ...m, status } : m),
    })),
    removeModel: (id) => set((state) => ({
        models: state.models.filter((m) => m.id !== id),
    })),
    setActiveModel: (id) => set({ activeModelId: id }),
    initializeRuntimes: async () => {
        const { runtimes } = get();
        for (const runtime of runtimes) {
            get().checkModelHealth(runtime.id);
        }
    },
    refreshPortals: async () => {
        set({ aiStatus: 'thinking' });
        try {
            // Use the shell bridge to call the jules CLI
            const { runShell } = await import('./ai-bridge');
            const output = await runShell('jules remote list --session');
            // Basic parser for the jules table output
            const lines = output.split('\n').filter(l => l.trim() && !l.startsWith('ID'));
            const newPortals = lines.map(line => {
                const parts = line.split(/\s{2,}/);
                return {
                    id: parts[0]?.trim() || '',
                    description: parts[1]?.trim() || '',
                    repo: parts[2]?.trim() || '',
                    lastActive: parts[3]?.trim() || '',
                    status: parts[4]?.trim() || 'Active'
                };
            });
            set({ portals: newPortals, aiStatus: 'idle' });
        }
        catch (error) {
            set({ aiStatus: 'error', aiError: 'Failed to fetch Portals from Nexium.' });
        }
    },
    generateBriefing: () => {
        const state = get();
        const activeProjectNode = state.nodes.find(n => n.id === state.activeProjectId && n.type === 'PROJECT');
        const allTasks = state.nodes.filter(n => n.type === 'TASK');
        const filteredTasks = state.activeProjectId
            ? allTasks.filter(t => state.edges.some(e => e.sourceId === state.activeProjectId && e.targetId === t.id))
            : allTasks;
        const allNotes = state.nodes.filter(n => n.type === 'IDEA');
        const filteredNotes = state.activeProjectId
            ? allNotes.filter(n => state.edges.some(e => e.sourceId === state.activeProjectId && e.targetId === n.id))
            : allNotes;
        let briefing = `# 🌌 GHOST NEXIUM PORTAL BRIEFING\n`;
        briefing += `Commander: Gemini CLI (via Continuum App)\n`;
        briefing += `Specialist: Jules\n\n`;
        briefing += `## 🎯 MISSION CONTEXT\n`;
        briefing += `Project: ${activeProjectNode?.title || 'Global Context'}\n`;
        briefing += `Status: Phase 10 (Master Synchronization)\n\n`;
        briefing += `## 📋 LOCAL TASK LIST\n`;
        filteredTasks.forEach(t => {
            briefing += `- [${t.data?.status === 'done' ? 'x' : ' '}] ${t.title}: ${t.description}\n`;
        });
        briefing += `\n## 🧠 RECENT THOUGHTS & NOTES\n`;
        filteredNotes.slice(-5).forEach(n => {
            briefing += `> ${n.data?.content || n.description}\n\n`;
        });
        briefing += `## 🚀 INSTRUCTION\n`;
        briefing += `Analyze the local state provided above and propose the next logical engineering steps to advance this project. Focus on implementing Task 10.3 (Result Ingestion) logic.`;
        return briefing;
    },
    openPortal: async (repo, briefing) => {
        set({ isThinking: true, aiStatus: 'thinking' });
        try {
            const { runShell } = await import('./ai-bridge');
            await runShell(`jules new --repo "${repo}" "${briefing.replace(/"/g, '\\"')}"`);
            await get().refreshPortals();
        }
        catch (error) {
            set({ aiStatus: 'error', aiError: 'Failed to open Nexium Portal.' });
        }
        finally {
            set({ isThinking: false });
        }
    },
    ingestPortalResults: async (portalId) => {
        set({ isThinking: true, aiStatus: 'thinking' });
        try {
            const { runShell } = await import('./ai-bridge');
            await runShell(`jules remote pull --session "${portalId}" --apply`);
            // Trigger a filesystem scan to pick up new files/changes
            const activeProject = get().projects.find(p => p.id === get().activeProjectId);
            if (activeProject?.path) {
                await get().scanFilesystem(activeProject.path);
            }
            await get().refreshPortals();
            set({ aiStatus: 'idle' });
        }
        catch (error) {
            set({ aiStatus: 'error', aiError: 'Failed to ingest Portal results.' });
        }
        finally {
            set({ isThinking: false });
        }
    },
    checkModelHealth: async (id) => {
        const state = get();
        const runtime = state.runtimes.find(r => r.id === id);
        if (!runtime)
            return false;
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 2000);
            const response = await fetch(`${runtime.baseUrl}/v1/models`, { signal: controller.signal });
            clearTimeout(timeoutId);
            const isOnline = response.ok;
            set((state) => ({
                runtimes: state.runtimes.map(r => r.id === id ? { ...r, status: isOnline ? 'online' : 'offline', lastChecked: Date.now() } : r)
            }));
            return isOnline;
        }
        catch {
            set((state) => ({
                runtimes: state.runtimes.map(r => r.id === id ? { ...r, status: 'offline', lastChecked: Date.now() } : r)
            }));
            return false;
        }
    },
    registerAgent: (agent) => set((state) => ({
        agents: [...state.agents, agent],
    })),
    updateAgent: (id, updates) => set((state) => ({
        agents: state.agents.map((a) => a.id === id ? { ...a, ...updates } : a),
    })),
    registerSkill: (skill) => set((state) => ({
        skills: [...state.skills, skill],
    })),
    externalRequest: (agentId, title, description, payload) => set((state) => ({
        suggestedActions: [...state.suggestedActions, {
                id: 'ext-' + Math.random().toString(36).substring(2, 9),
                projectId: state.activeProjectId || 'global',
                title, description, status: SuggestedActionStatus.ACTIVE, actionType: 'other', metadata: payload, createdAt: Date.now(), updatedAt: Date.now(),
            }]
    })),
    scanFilesystem: async (rootPath) => {
        set({ isScanning: true });
        await new Promise((resolve) => setTimeout(resolve, 1500));
        const mockFoundProject = {
            id: 'mock-' + Math.random().toString(36).substring(2, 9),
            name: 'Project X (Scanned)', description: 'Automatically discovered via filesystem scan',
            path: `${rootPath}/ProjectX`, createdAt: Date.now(), updatedAt: Date.now(), tags: ['auto-scanned'],
        };
        set((state) => {
            const exists = state.projects.some(p => p.path === mockFoundProject.path);
            if (exists)
                return { isScanning: false, lastSyncTimestamp: Date.now() };
            return { projects: [...state.projects, mockFoundProject], isScanning: false, lastSyncTimestamp: Date.now() };
        });
    },
    startBackgroundScan: (rootPath, intervalMs) => {
        if (window.continuumScanInterval)
            clearInterval(window.continuumScanInterval);
        get().scanFilesystem(rootPath);
        window.continuumScanInterval = setInterval(() => { get().scanFilesystem(rootPath); }, intervalMs);
    },
    stopBackgroundScan: () => {
        if (window.continuumScanInterval) {
            clearInterval(window.continuumScanInterval);
            window.continuumScanInterval = null;
        }
    },
    proposeMutation: (mutation) => set((state) => {
        let isOutOfScope = false;
        let isUnauthorized = false;
        const { activeProjectId, agents, skills } = state;
        if (mutation.agentId) {
            const agent = agents.find((a) => a.id === mutation.agentId);
            if (!agent) {
                isUnauthorized = true;
            }
            else {
                const agentSkills = skills.filter((s) => agent.skillIds.includes(s.id));
                const allPermissions = agentSkills.flatMap((s) => s.permissions);
                if (!allPermissions.includes(`WRITE_${mutation.entity}`)) {
                    isUnauthorized = true;
                }
            }
        }
        if (activeProjectId) {
            const { type, entity, payload } = mutation;
            if (entity === 'PROJECT') {
                if (type !== 'CREATE' && payload.id !== activeProjectId)
                    isOutOfScope = true;
            }
            else {
                const targetProjectId = payload.projectId;
                if (targetProjectId) {
                    if (targetProjectId !== activeProjectId)
                        isOutOfScope = true;
                }
                else if (type !== 'CREATE') {
                    let existingEntity;
                    if (entity === 'TASK')
                        existingEntity = state.tasks.find(t => t.id === payload.id);
                    if (entity === 'NOTE')
                        existingEntity = state.notes.find(n => n.id === payload.id);
                    if (entity === 'ACTION')
                        existingEntity = state.suggestedActions.find(a => a.id === payload.id);
                    if (existingEntity && existingEntity.projectId !== activeProjectId)
                        isOutOfScope = true;
                    else if (entity === 'NOTE' && !existingEntity?.projectId)
                        isOutOfScope = true;
                }
                else if (type === 'CREATE')
                    isOutOfScope = true;
            }
        }
        return {
            pendingMutations: [...state.pendingMutations, {
                    ...mutation, id: crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 15),
                    timestamp: Date.now(), isOutOfScope, isUnauthorized,
                }],
        };
    }),
    commitMutation: (id, force = false) => {
        const mutation = get().pendingMutations.find((m) => m.id === id);
        if (!mutation)
            return;
        if (mutation.isOutOfScope && !force) {
            console.warn('Blocked out-of-scope mutation. Use force=true to override.');
            return;
        }
        const currentState = get();
        const { undoHistory, ...snapshot } = currentState;
        const { type, entity, payload } = mutation;
        set((state) => {
            let newState = { ...state };
            newState.undoHistory = [{ snapshot, timestamp: Date.now() }, ...state.undoHistory].slice(0, 50);
            if (entity === 'TASK') {
                if (type === 'CREATE')
                    newState.tasks = [...state.tasks, payload];
                if (type === 'UPDATE')
                    newState.tasks = state.tasks.map(t => t.id === payload.id ? { ...t, ...payload } : t);
                if (type === 'DELETE')
                    newState.tasks = state.tasks.filter(t => t.id !== payload.id);
            }
            else if (entity === 'PROJECT') {
                if (type === 'CREATE')
                    newState.projects = [...state.projects, payload];
                if (type === 'UPDATE')
                    newState.projects = state.projects.map(p => p.id === payload.id ? { ...p, ...payload } : p);
                if (type === 'DELETE')
                    newState.projects = state.projects.filter(p => p.id !== payload.id);
            }
            else if (entity === 'NOTE') {
                if (type === 'CREATE')
                    newState.notes = [...state.notes, payload];
                if (type === 'UPDATE')
                    newState.notes = state.notes.map(n => n.id === payload.id ? { ...n, ...payload } : n);
                if (type === 'DELETE')
                    newState.notes = state.notes.filter(n => n.id !== payload.id);
            }
            else if (entity === 'ACTION') {
                if (type === 'CREATE')
                    newState.suggestedActions = [...state.suggestedActions, payload];
                if (type === 'UPDATE')
                    newState.suggestedActions = state.suggestedActions.map(a => a.id === payload.id ? { ...a, ...payload } : a);
                if (type === 'DELETE')
                    newState.suggestedActions = state.suggestedActions.filter(a => a.id !== payload.id);
            }
            newState.pendingMutations = state.pendingMutations.filter((m) => m.id !== id);
            return newState;
        });
    },
    rejectMutation: (id) => set((state) => ({
        pendingMutations: state.pendingMutations.filter((m) => m.id !== id),
    })),
    undo: () => {
        const lastUndo = get().undoHistory[0];
        if (!lastUndo)
            return;
        set((state) => ({ ...lastUndo.snapshot, undoHistory: state.undoHistory.slice(1) }));
    },
    setLastSync: (timestamp) => set({ lastSyncTimestamp: timestamp }),
    resetStore: () => set(initialState),
    completeSetup: () => set({ hasCompletedSetup: true }),
}), {
    name: 'continuum-storage',
    storage: createJSONStorage(() => capacitorStorage),
    partialize: (state) => ({
        nodes: state.nodes,
        edges: state.edges,
        layout: state.layout,
        projects: state.projects,
        tasks: state.tasks,
        notes: state.notes,
        models: state.models,
        agents: state.agents,
        skills: state.skills,
        activeProjectId: state.activeProjectId,
        activeModelId: state.activeModelId,
        aiRequestQueue: state.aiRequestQueue,
        isThinking: state.isThinking,
        aiStatus: state.aiStatus,
        aiError: state.aiError,
        portals: state.portals,
        hasCompletedSetup: state.hasCompletedSetup,
    }),
}));
// Initialize runtimes on boot
useContinuumStore.getState().initializeRuntimes();
//# sourceMappingURL=store.js.map