import { create } from 'zustand';
import { 
  type ContinuumState, 
  type Project, 
  type Task, 
  type IdealNote, 
  type SuggestedAction,
  type Mutation,
  SuggestedActionStatus,
  type AIModel,
  type Agent,
  type Skill,
  type Node,
  type Edge,
  type AIRuntime,
  type PromptTemplate
} from './types';
import { callLocalLLM } from './ai-bridge';

interface ContinuumActions {
  // Graph Actions
  addNode: (node: Node) => void;
  updateNode: (id: string, updates: Partial<Node>) => void;
  deleteNode: (id: string) => void;
  addEdge: (edge: Edge) => void;
  removeEdge: (id: string) => void;
  migrateToGraph: () => void;

  // AI Infrastructure Actions
  registerRuntime: (runtime: AIRuntime) => void;
  updateRuntimeStatus: (id: string, status: AIRuntime['status']) => void;
  addPromptTemplate: (template: PromptTemplate) => void;
  updateDownloadProgress: (modelId: string, progress: number) => void;
  requestAI: (agentId: string, templateId: string, context: any) => Promise<void>;
  downloadModel: (modelId: string) => Promise<void>;

  // Layout Actions
  updateNodePosition: (nodeId: string, x: number, y: number) => void;
  updateViewport: (zoom: number, pan: { x: number; y: number }) => void;

  addProject: (project: Project) => void;
  updateProject: (id: string, updates: Partial<Project>) => void;
  setActiveProject: (id: string | null) => void;
  
  addTask: (task: Task) => void;
  updateTask: (id: string, updates: Partial<Task>) => void;
  deleteTask: (id: string) => void;
  
  addNote: (note: IdealNote) => void;
  updateNote: (id: string, updates: Partial<IdealNote>) => void;
  
  addSuggestedAction: (action: SuggestedAction) => void;
  updateSuggestedAction: (id: string, updates: Partial<SuggestedAction>) => void;
  snoozeAction: (id: string, durationMinutes?: number) => void;
  archiveAction: (id: string) => void;
  acceptAction: (id: string) => void;
  deleteSuggestedAction: (id: string) => void;
  resurfaceActions: () => void;

  // Model Registry
  registerModel: (model: AIModel) => void;
  updateModelStatus: (id: string, status: AIModel['status']) => void;
  removeModel: (id: string) => void;
  setActiveModel: (id: string | null) => void;
  checkModelHealth: (id: string) => Promise<boolean>;

  // Agents & Skills
  registerAgent: (agent: Agent) => void;
  updateAgent: (id: string, updates: Partial<Agent>) => void;
  registerSkill: (skill: Skill) => void;
  externalRequest: (agentId: string, title: string, description: string, payload: any) => void;

  // Filesystem Integration
  scanFilesystem: (rootPath: string) => Promise<void>;
  startBackgroundScan: (rootPath: string, intervalMs: number) => void;
  stopBackgroundScan: () => void;
  
  // Mutation Pipeline
  proposeMutation: (mutation: Omit<Mutation, 'id' | 'timestamp' | 'isOutOfScope'>) => void;
  commitMutation: (id: string, force?: boolean) => void;
  rejectMutation: (id: string) => void;
  undo: () => void;

  setLastSync: (timestamp: number) => void;
  resetStore: () => void;
}

const initialState: ContinuumState = {
  nodes: [],
  edges: [],
  layout: { positions: {}, zoom: 1, pan: { x: 0, y: 0 } },
  runtimes: [],
  promptTemplates: [],
  downloadProgress: {},
  projects: [],
  tasks: [],
  notes: [],
  suggestedActions: [],
  models: [],
  agents: [],
  skills: [],
  pendingMutations: [],
  undoHistory: [],
  activeProjectId: null,
  activeModelId: null,
  lastSyncTimestamp: 0,
  isScanning: false,
};

export const useContinuumStore = create<ContinuumState & ContinuumActions>((set, get) => {
  let scanIntervalId: any = null;

  return {
    ...initialState,

    addNode: (node: Node) => set((state: ContinuumState) => ({
      nodes: [...state.nodes, node],
    })),

    updateNode: (id: string, updates: Partial<Node>) => set((state: ContinuumState) => ({
      nodes: state.nodes.map((n) => n.id === id ? { ...n, ...updates, updatedAt: Date.now() } : n),
    })),

    deleteNode: (id: string) => set((state: ContinuumState) => ({
      nodes: state.nodes.filter((n) => n.id !== id),
      edges: state.edges.filter((e) => e.sourceId !== id && e.targetId !== id),
    })),

    addEdge: (edge: Edge) => set((state: ContinuumState) => ({
      edges: [...state.edges, edge],
    })),

    removeEdge: (id: string) => set((state: ContinuumState) => ({
      edges: state.edges.filter((e) => e.id !== id),
    })),

    migrateToGraph: () => {
      const { projects, tasks, notes } = get();
      const newNodes: Node[] = [];
      const newEdges: Edge[] = [];

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
        newNodes.push({
          id: n.id, type: 'IDEA', title: 'Ideal Note', description: n.content.substring(0, 50) + '...',
          data: { content: n.content, tags: n.tags }, metadata: {}, createdAt: n.createdAt, updatedAt: n.updatedAt
        });
        if (n.projectId) {
          newEdges.push({ id: `edge-${n.projectId}-${n.id}`, sourceId: n.projectId, targetId: n.id, type: 'PARENT_OF', metadata: {}, createdAt: Date.now() });
        }
      });

      set({ nodes: newNodes, edges: newEdges });
    },

    registerRuntime: (runtime: AIRuntime) => set((state: ContinuumState) => ({
      runtimes: [...state.runtimes, runtime],
    })),

    updateRuntimeStatus: (id: string, status: AIRuntime['status']) => set((state: ContinuumState) => ({
      runtimes: state.runtimes.map((r) => r.id === id ? { ...r, status, lastChecked: Date.now() } : r),
    })),

    addPromptTemplate: (template: PromptTemplate) => set((state: ContinuumState) => ({
      promptTemplates: [...state.promptTemplates, template],
    })),

    updateDownloadProgress: (modelId: string, progress: number) => set((state: ContinuumState) => ({
      downloadProgress: { ...state.downloadProgress, [modelId]: progress },
    })),

    requestAI: async (agentId: string, templateId: string, context: any) => {
      const state = get();
      const agent = state.agents.find(a => a.id === agentId);
      const template = state.promptTemplates.find(t => t.id === templateId);
      const runtime = state.runtimes.find(r => r.status === 'online');

      if (!agent || !template || !runtime) {
        console.error('AI Request failed: Missing agent, template, or online runtime.');
        return;
      }

      try {
        const userPrompt = template.userPromptFormat.replace('{{content}}', JSON.stringify(context));
        const rawResponse = await callLocalLLM(runtime, template.systemPrompt, userPrompt);
        const jsonMatch = rawResponse.match(/\{[\s\S]*\}/);
        if (jsonMatch) {
          const mutationPayload = JSON.parse(jsonMatch[0]);
          get().proposeMutation({
            type: mutationPayload.type || 'CREATE',
            entity: mutationPayload.entity || 'TASK',
            payload: mutationPayload.payload,
            agentId: agent.id
          });
        }
      } catch (error) {
        console.error('Inference error:', error);
      }
    },

    downloadModel: async (modelId: string) => {
      const model = get().models.find(m => m.id === modelId);
      if (!model || !model.downloadUrl) return;
      let progress = 0;
      const interval = setInterval(() => {
        progress += 10;
        get().updateDownloadProgress(modelId, progress);
        if (progress >= 100) {
          clearInterval(interval);
          set((state: ContinuumState) => ({
            models: state.models.map(m => m.id === modelId ? { ...m, status: 'available' } : m)
          }));
        }
      }, 500);
    },

    updateNodePosition: (nodeId: string, x: number, y: number) => set((state: ContinuumState) => ({
      layout: {
        ...state.layout,
        positions: {
          ...state.layout.positions,
          [nodeId]: { x, y }
        }
      }
    })),

    updateViewport: (zoom: number, pan: { x: number; y: number }) => set((state: ContinuumState) => ({
      layout: { ...state.layout, zoom, pan }
    })),

    addProject: (project: Project) => set((state: ContinuumState) => ({
      projects: [...state.projects, project],
    })),

    updateProject: (id: string, updates: Partial<Project>) => set((state: ContinuumState) => ({
      projects: state.projects.map((p) => p.id === id ? { ...p, ...updates, updatedAt: Date.now() } : p),
    })),

    setActiveProject: (id: string | null) => set({ activeProjectId: id }),

    addTask: (task: Task) => set((state: ContinuumState) => ({
      tasks: [...state.tasks, task],
    })),

    updateTask: (id: string, updates: Partial<Task>) => set((state: ContinuumState) => ({
      tasks: state.tasks.map((t) => t.id === id ? { ...t, ...updates, updatedAt: Date.now() } : t),
    })),

    deleteTask: (id: string) => set((state: ContinuumState) => ({
      tasks: state.tasks.filter((t) => t.id !== id),
    })),

    addNote: (note: IdealNote) => set((state: ContinuumState) => ({
      notes: [...state.notes, note],
    })),

    updateNote: (id: string, updates: Partial<IdealNote>) => set((state: ContinuumState) => ({
      notes: state.notes.map((n) => n.id === id ? { ...n, ...updates, updatedAt: Date.now() } : n),
    })),

    addSuggestedAction: (action: SuggestedAction) => set((state: ContinuumState) => ({
      suggestedActions: [...state.suggestedActions, action],
    })),

    updateSuggestedAction: (id: string, updates: Partial<SuggestedAction>) => set((state: ContinuumState) => ({
      suggestedActions: state.suggestedActions.map((a) => a.id === id ? { ...a, ...updates, updatedAt: Date.now() } : a),
    })),

    snoozeAction: (id: string, durationMinutes?: number) => set((state: ContinuumState) => ({
      suggestedActions: state.suggestedActions.map((a) => a.id === id ? { 
        ...a, 
        status: SuggestedActionStatus.SNOOZED, 
        snoozedUntil: durationMinutes ? Date.now() + (durationMinutes * 60 * 1000) : undefined,
        updatedAt: Date.now() 
      } : a),
    })),

    archiveAction: (id: string) => set((state: ContinuumState) => ({
      suggestedActions: state.suggestedActions.map((a) => a.id === id ? { 
        ...a, 
        status: SuggestedActionStatus.ARCHIVED, 
        snoozedUntil: undefined,
        updatedAt: Date.now() 
      } : a),
    })),

    acceptAction: (id: string) => {
      const action = get().suggestedActions.find((a) => a.id === id);
      if (!action || !action.metadata) return;
      set((state: ContinuumState) => ({
        suggestedActions: state.suggestedActions.map((a) => a.id === id ? { ...a, status: SuggestedActionStatus.ACCEPTED, snoozedUntil: undefined, updatedAt: Date.now() } : a),
      }));
      const { actionType, metadata } = action;
      let entity: Mutation['entity'] = 'TASK';
      let type: Mutation['type'] = 'CREATE';
      if (actionType === 'update_task' || actionType === 'refactor') type = 'UPDATE';
      if (actionType !== 'other') {
        get().proposeMutation({ type, entity, payload: metadata, agentId: 'continuum-suggester' });
      }
    },

    deleteSuggestedAction: (id: string) => set((state: ContinuumState) => ({
      suggestedActions: state.suggestedActions.filter((a) => a.id !== id),
    })),

    resurfaceActions: () => {
      const now = Date.now();
      set((state: ContinuumState) => ({
        suggestedActions: state.suggestedActions.map((a) => {
          if (a.status === SuggestedActionStatus.SNOOZED && a.snoozedUntil && a.snoozedUntil <= now) {
            return { ...a, status: SuggestedActionStatus.ACTIVE, snoozedUntil: undefined, updatedAt: now };
          }
          return a;
        })
      }));
    },

    registerModel: (model: AIModel) => set((state: ContinuumState) => ({
      models: [...state.models, model],
    })),

    updateModelStatus: (id: string, status: AIModel['status']) => set((state: ContinuumState) => ({
      models: state.models.map((m) => m.id === id ? { ...m, status } : m),
    })),

    removeModel: (id: string) => set((state: ContinuumState) => ({
      models: state.models.filter((m) => m.id !== id),
    })),

    setActiveModel: (id: string | null) => set({ activeModelId: id }),

    checkModelHealth: async (id: string) => {
      const model = get().models.find((m) => m.id === id);
      if (!model) return false;
      await new Promise((resolve) => setTimeout(resolve, 800));
      const isHealthy = Math.random() > 0.1;
      const status = isHealthy ? 'available' : 'missing';
      set((state: ContinuumState) => ({ models: state.models.map((m) => m.id === id ? { ...m, status } : m) }));
      return isHealthy;
    },

    registerAgent: (agent: Agent) => set((state: ContinuumState) => ({
      agents: [...state.agents, agent],
    })),

    updateAgent: (id: string, updates: Partial<Agent>) => set((state: ContinuumState) => ({
      agents: state.agents.map((a) => a.id === id ? { ...a, ...updates } : a),
    })),

    registerSkill: (skill: Skill) => set((state: ContinuumState) => ({
      skills: [...state.skills, skill],
    })),

    externalRequest: (agentId: string, title: string, description: string, payload: any) => set((state: ContinuumState) => ({
      suggestedActions: [...state.suggestedActions, {
        id: 'ext-' + Math.random().toString(36).substring(2, 9),
        projectId: state.activeProjectId || 'global',
        title, description, status: SuggestedActionStatus.ACTIVE, actionType: 'other', metadata: payload, createdAt: Date.now(), updatedAt: Date.now(),
      }]
    })),

    scanFilesystem: async (rootPath: string) => {
      set({ isScanning: true });
      await new Promise((resolve) => setTimeout(resolve, 1500));
      const mockFoundProject: Project = {
        id: 'mock-' + Math.random().toString(36).substring(2, 9),
        name: 'Project X (Scanned)', description: 'Automatically discovered via filesystem scan',
        path: `${rootPath}/ProjectX`, createdAt: Date.now(), updatedAt: Date.now(), tags: ['auto-scanned'],
      };
      set((state: ContinuumState) => {
        const exists = state.projects.some(p => p.path === mockFoundProject.path);
        if (exists) return { isScanning: false, lastSyncTimestamp: Date.now() };
        return { projects: [...state.projects, mockFoundProject], isScanning: false, lastSyncTimestamp: Date.now() };
      });
    },

    startBackgroundScan: (rootPath: string, intervalMs: number) => {
      if (scanIntervalId) clearInterval(scanIntervalId);
      get().scanFilesystem(rootPath);
      scanIntervalId = setInterval(() => { get().scanFilesystem(rootPath); }, intervalMs);
    },

    stopBackgroundScan: () => {
      if (scanIntervalId) { clearInterval(scanIntervalId); scanIntervalId = null; }
    },

    proposeMutation: (mutation: Omit<Mutation, 'id' | 'timestamp' | 'isOutOfScope'>) => set((state: ContinuumState) => {
      let isOutOfScope = false; let isUnauthorized = false;
      const { activeProjectId, agents, skills } = state;
      if (mutation.agentId) {
        const agent = agents.find((a) => a.id === mutation.agentId);
        if (!agent) { isUnauthorized = true; } 
        else {
          const agentSkills = skills.filter((s) => agent.skillIds.includes(s.id));
          const allPermissions = agentSkills.flatMap((s) => s.permissions);
          if (!allPermissions.includes(`WRITE_${mutation.entity}`)) { isUnauthorized = true; }
        }
      }
      if (activeProjectId) {
        const { type, entity, payload } = mutation;
        if (entity === 'PROJECT') { if (type !== 'CREATE' && payload.id !== activeProjectId) isOutOfScope = true; } 
        else {
          const targetProjectId = payload.projectId;
          if (targetProjectId) { if (targetProjectId !== activeProjectId) isOutOfScope = true; } 
          else if (type !== 'CREATE') {
            let existingEntity;
            if (entity === 'TASK') existingEntity = state.tasks.find(t => t.id === payload.id);
            if (entity === 'NOTE') existingEntity = state.notes.find(n => n.id === payload.id);
            if (entity === 'ACTION') existingEntity = state.suggestedActions.find(a => a.id === payload.id);
            if (existingEntity && existingEntity.projectId !== activeProjectId) isOutOfScope = true;
            else if (entity === 'NOTE' && !(existingEntity as any)?.projectId) isOutOfScope = true;
          } else if (type === 'CREATE') isOutOfScope = true;
        }
      }
      return {
        pendingMutations: [...state.pendingMutations, {
          ...mutation, id: crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 15),
          timestamp: Date.now(), isOutOfScope, isUnauthorized,
        }],
      };
    }),

    commitMutation: (id: string, force = false) => {
      const mutation = get().pendingMutations.find((m) => m.id === id);
      if (!mutation) return;
      if (mutation.isOutOfScope && !force) { console.warn('Blocked out-of-scope mutation. Use force=true to override.'); return; }
      const currentState = get();
      const { undoHistory, ...snapshot } = currentState;
      const { type, entity, payload } = mutation;
      set((state: ContinuumState) => {
        let newState = { ...state };
        newState.undoHistory = [{ snapshot, timestamp: Date.now() }, ...state.undoHistory].slice(0, 50);
        if (entity === 'TASK') {
          if (type === 'CREATE') newState.tasks = [...state.tasks, payload];
          if (type === 'UPDATE') newState.tasks = state.tasks.map(t => t.id === payload.id ? { ...t, ...payload } : t);
          if (type === 'DELETE') newState.tasks = state.tasks.filter(t => t.id !== payload.id);
        } else if (entity === 'PROJECT') {
          if (type === 'CREATE') newState.projects = [...state.projects, payload];
          if (type === 'UPDATE') newState.projects = state.projects.map(p => p.id === payload.id ? { ...p, ...payload } : p);
          if (type === 'DELETE') newState.projects = state.projects.filter(p => p.id !== payload.id);
        } else if (entity === 'NOTE') {
          if (type === 'CREATE') newState.notes = [...state.notes, payload];
          if (type === 'UPDATE') newState.notes = state.notes.map(n => n.id === payload.id ? { ...n, ...payload } : n);
          if (type === 'DELETE') newState.notes = state.notes.filter(n => n.id !== payload.id);
        } else if (entity === 'ACTION') {
          if (type === 'CREATE') newState.suggestedActions = [...state.suggestedActions, payload];
          if (type === 'UPDATE') newState.suggestedActions = state.suggestedActions.map(a => a.id === payload.id ? { ...a, ...payload } : a);
          if (type === 'DELETE') newState.suggestedActions = state.suggestedActions.filter(a => a.id !== payload.id);
        }
        newState.pendingMutations = state.pendingMutations.filter((m) => m.id !== id);
        return newState;
      });
    },

    rejectMutation: (id: string) => set((state: ContinuumState) => ({
      pendingMutations: state.pendingMutations.filter((m) => m.id !== id),
    })),

    undo: () => {
      const lastUndo = get().undoHistory[0];
      if (!lastUndo) return;
      set((state: ContinuumState) => ({ ...lastUndo.snapshot, undoHistory: state.undoHistory.slice(1) } as any));
    },

    setLastSync: (timestamp: number) => set({ lastSyncTimestamp: timestamp }),
    resetStore: () => set(initialState),
  };
});
