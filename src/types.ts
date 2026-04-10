/**
 * Continuum Core Data Models
 */

export enum SuggestedActionStatus {
  ACTIVE = 'active',
  SNOOZED = 'snoozed',
  ARCHIVED = 'archived',
  ACCEPTED = 'accepted',
}

export enum TaskStatus {
  TODO = 'todo',
  IN_PROGRESS = 'in_progress',
  DONE = 'done',
  BLOCKED = 'blocked',
}

export type NodeType = 'PROJECT' | 'TASK' | 'IDEA' | 'GOAL' | 'AGENT';

export interface Node {
  id: string;
  type: NodeType;
  title: string;
  description: string;
  data: any; // Flexible payload depending on type
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
  locked?: boolean; // If true, auto-layout won't move this node
}

export interface GraphLayout {
  positions: Record<string, NodePosition>; // nodeId -> {x, y}
  zoom: number;
  pan: { x: number; y: number };
}

export interface Project {
  id: string;
  name: string;
  description: string;
  createdAt: number;
  updatedAt: number;
  path: string; // File system path
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
  projectId?: string | undefined; // Optional if not linked to a project yet
  content: string; // Markdown content
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
  metadata?: Record<string, any> | undefined; // Flexible storage for AI-generated payloads
  snoozedUntil?: number | undefined; // Optional timestamp until when the action is snoozed
}

export interface AIModel {
  id: string;
  name: string;
  version: string;
  provider: 'local' | 'external';
  filePath?: string | undefined; // For local GGUF
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
  baseUrl: string; // e.g., http://127.0.0.1:4891
  status: 'online' | 'offline' | 'unknown';
  lastChecked: number;
}

export interface PromptTemplate {
  id: string;
  name: string;
  systemPrompt: string;
  userPromptFormat: string; // Placeholder string like "Process this note: {{content}}"
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
  modelId?: string | undefined; // Optional override for the global activeModelId
  status: 'active' | 'inactive';
}

export interface Mutation {
  id: string;
  type: 'CREATE' | 'UPDATE' | 'DELETE';
  entity: 'PROJECT' | 'TASK' | 'NOTE' | 'ACTION';
  payload: any;
  timestamp: number;
  agentId?: string; // Which agent proposed this?
  isOutOfScope?: boolean; // Does this mutation affect a project other than the active one?
  isUnauthorized?: boolean; // Does the agent have the necessary permissions?
}

export interface ContinuumState {
  nodes: Node[];
  edges: Edge[];
  layout: GraphLayout;
  runtimes: AIRuntime[];
  promptTemplates: PromptTemplate[];
  downloadProgress: Record<string, number>; // modelId -> percentage
  projects: Project[];
  tasks: Task[];
  notes: IdealNote[];
  suggestedActions: SuggestedAction[];
  models: AIModel[]; // AI Model Registry
  agents: Agent[]; // Agent Registry
  skills: Skill[]; // Skill Registry
  pendingMutations: Mutation[]; // Queue for user approval
  undoHistory: { snapshot: Omit<ContinuumState, 'undoHistory'>; timestamp: number }[];
  activeProjectId: string | null;
  activeModelId: string | null;
  lastSyncTimestamp: number;
  isScanning: boolean;
}
