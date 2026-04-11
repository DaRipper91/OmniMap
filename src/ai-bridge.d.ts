import { type AIRuntime, type AIModel } from './types';
/**
 * AI Bridge for Project Singularity
 * Handles low-level communication with LLM runtimes.
 */
export declare const callLocalLLM: (runtime: AIRuntime, systemPrompt: string, userPrompt: string) => Promise<string>;
export declare const fetchModelCatalog: () => Promise<AIModel[]>;
export declare const checkRuntime: (runtime: AIRuntime) => Promise<boolean>;
/**
 * Executes a shell command via the Nexium Bridge.
 * (In a native Capacitor app, this would use a custom plugin or the Capacitor HTTP/Filesystem bridge).
 */
export declare const runShell: (command: string) => Promise<string>;
//# sourceMappingURL=ai-bridge.d.ts.map