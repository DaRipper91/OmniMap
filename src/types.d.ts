/**
 * Continuum Core Data Models
 */
export declare enum SuggestedActionStatus {
    ACTIVE = "active",
    SNOOZED = "snoozed",
    ARCHIVED = "archived",
    ACCEPTED = "accepted"
}
export declare enum TaskStatus {
    TODO = "todo",
    IN_PROGRESS = "in_progress",
    DONE = "done",
    BLOCKED = "blocked"
}
export type NodeType = 'PROJECT' | 'TASK' | 'IDEA' | 'GOAL' | 'AGENT';
export interface Node {
    id: string;
    type: NodeType;
    title: string;
    description: string;
    data: any;
    metadata: Record<string, any>;
    createdAt: number;
    updatedAt: number;
}
export type EdgeType = 'PARENT_OF' | 'DEPENDS_ON' | 'RELATED_TO' | 'CREATED_BY';
export interface Edge {
    id: string;
    sourceId: string;
    targetId: string;
    type: EdgeType;
    metadata: Record<string, any>;
    createdAt: number;
}
export interface NodePosition {
    x: number;
    y: number;
    locked?: boolean;
}
export interface GraphLayout {
    positions: Record<string, NodePosition>;
    zoom: number;
    pan: {
        x: number;
        y: number;
    };
}
export interface Project {
    id: string;
    name: string;
    description: string;
    createdAt: number;
    updatedAt: number;
    path: string;
    tags: string[];
}
export interface Task {
    id: string;
    projectId: string;
    title: string;
    description: string;
    status: TaskStatus;
    priority: number;
    createdAt: number;
    updatedAt: number;
    dueDate?: number | undefined;
    tags: string[];
}
export interface IdealNote {
    id: string;
    projectId?: string | undefined;
    content: string;
    createdAt: number;
    updatedAt: number;
    tags: string[];
}
export interface SuggestedAction {
    id: string;
    projectId: string;
    title: string;
    description: string;
    status: SuggestedActionStatus;
    createdAt: number;
    updatedAt: number;
    actionType: 'create_task' | 'create_note' | 'update_task' | 'refactor' | 'other';
    metadata?: Record<string, any> | undefined;
    snoozedUntil?: number | undefined;
}
export interface AIModel {
    id: string;
    name: string;
    version: string;
    provider: 'local' | 'external';
    filePath?: string | undefined;
    sizeGB?: number | undefined;
    engine: 'llama.cpp' | 'gpt4all' | 'openai';
    capabilities: ('text' | 'vision' | 'code' | 'planning')[];
    status: 'available' | 'downloading' | 'missing';
    downloadUrl?: string | undefined;
    checksum?: string | undefined;
}
export interface AIRuntime {
    id: string;
    name: string;
    baseUrl: string;
    status: 'online' | 'offline' | 'unknown';
    lastChecked: number;
}
export interface PromptTemplate {
    id: string;
    name: string;
    systemPrompt: string;
    userPromptFormat: string;
}
export interface Skill {
    id: string;
    name: string;
    description: string;
    type: 'internal' | 'external';
    permissions: string[];
}
export interface Agent {
    id: string;
    name: string;
    persona: string;
    role: string;
    skillIds: string[];
    modelId?: string | undefined;
    status: 'active' | 'inactive';
}
export interface Mutation {
    id: string;
    type: 'CREATE' | 'UPDATE' | 'DELETE';
    entity: 'PROJECT' | 'TASK' | 'NOTE' | 'ACTION';
    payload: any;
    timestamp: number;
    agentId?: string;
    isOutOfScope?: boolean;
    isUnauthorized?: boolean;
}
export interface QueuedAIRequest {
    id: string;
    agentId: string;
    templateId: string;
    context: any;
    status: 'pending' | 'failed';
    timestamp: number;
    retryCount: number;
}
export interface NexiumPortal {
    id: string;
    description: string;
    repo: string;
    lastActive: string;
    status: string;
}
export interface ContinuumState {
    nodes: Node[];
    edges: Edge[];
    layout: GraphLayout;
    runtimes: AIRuntime[];
    promptTemplates: PromptTemplate[];
    downloadProgress: Record<string, number>;
    projects: Project[];
    tasks: Task[];
    notes: IdealNote[];
    suggestedActions: SuggestedAction[];
    models: AIModel[];
    agents: Agent[];
    skills: Skill[];
    pendingMutations: Mutation[];
    undoHistory: {
        snapshot: Omit<ContinuumState, 'undoHistory'>;
        timestamp: number;
    }[];
    activeProjectId: string | null;
    activeModelId: string | null;
    lastSyncTimestamp: number;
    isScanning: boolean;
    isThinking: boolean;
    aiStatus: 'idle' | 'thinking' | 'retrying' | 'error';
    aiError: string | null;
    hasCompletedSetup: boolean;
    portals: NexiumPortal[];
    aiRequestQueue: QueuedAIRequest[];
}
//# sourceMappingURL=types.d.ts.map