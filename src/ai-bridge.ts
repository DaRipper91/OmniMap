import { type AIRuntime, type AIModel } from './types';

/**
 * AI Bridge for Project Singularity
 * Handles low-level communication with local LLM runtimes.
 */

export const callLocalLLM = async (
  runtime: AIRuntime, 
  systemPrompt: string, 
  userPrompt: string
): Promise<string> => {
  try {
    const response = await fetch(`${runtime.baseUrl}/v1/chat/completions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model: 'default', // Local runtimes usually ignore this or have a default
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: userPrompt }
        ],
        temperature: 0.7,
        stream: false
      })
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const data = await response.json();
    return data.choices[0]?.message?.content || '';
  } catch (error) {
    console.error('Failed to call local LLM:', error);
    throw error;
  }
};

export const fetchModelCatalog = async (): Promise<AIModel[]> => {
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

export const checkRuntime = async (runtime: AIRuntime): Promise<boolean> => {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 2000);
    
    const response = await fetch(`${runtime.baseUrl}/v1/models`, { 
      signal: controller.signal 
    });
    
    clearTimeout(timeoutId);
    return response.ok;
  } catch {
    return false;
  }
};
