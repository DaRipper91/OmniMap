import {} from './types';
import { geminiService } from './gemini-service';
/**
 * AI Bridge for Project Singularity
 * Handles low-level communication with LLM runtimes.
 */
export const callLocalLLM = async (runtime, systemPrompt, userPrompt) => {
    try {
        return await geminiService.generateContent(systemPrompt, userPrompt);
    }
    catch (error) {
        console.error('Failed to call Gemini API:', error);
        throw error;
    }
};
export const fetchModelCatalog = async () => {
    // In a real app, this would be a remote JSON. Mocking for now.
    return [
        {
            id: 'llama-3-8b-q4',
            name: 'LLaMA 3 8B (Quantized)',
            version: '1.0',
            provider: 'local',
            engine: 'llama.cpp',
            sizeGB: 4.2,
            capabilities: ['text', 'code', 'planning'],
            status: 'missing',
            downloadUrl: 'https://huggingface.co/meta-llama/Meta-Llama-3-8B-GGUF/resolve/main/Meta-Llama-3-8B-Q4_K_M.gguf'
        }
    ];
};
export const checkRuntime = async (runtime) => {
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 2000);
        const response = await fetch(`${runtime.baseUrl}/v1/models`, {
            signal: controller.signal
        });
        clearTimeout(timeoutId);
        return response.ok;
    }
    catch {
        return false;
    }
};
/**
 * Executes a shell command via the Nexium Bridge.
 * (In a native Capacitor app, this would use a custom plugin or the Capacitor HTTP/Filesystem bridge).
 */
export const runShell = async (command) => {
    // Mocking the bridge for the web environment. 
    // In the real Ghost Nexium, this calls out to the Termux environment.
    console.log(`[Nexium-Bridge] Executing: ${command}`);
    // Return a mock output for the web environment to satisfy the store logic.
    if (command.includes('remote list')) {
        return `ID                                    Description                                    Repo                Last active                Status         
 12557025922133699993    Implement Gemini API Pivot                     DaRipper91/contiinuum   2m ago                  Awaiting User F
 17532370895457137341    Robust Error Recovery Logic                    DaRipper91/contiinuum   1h ago                  Thinking`;
    }
    if (command.includes('remote pull')) {
        console.log(`[Nexium-Bridge] Pulling results for portal...`);
        return "Patch applied successfully.";
    }
    return "Command executed successfully.";
};
//# sourceMappingURL=ai-bridge.js.map