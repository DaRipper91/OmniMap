/**
 * Service for interacting with Gemini API.
 */
export declare class GeminiService {
    private genAI;
    private model;
    constructor();
    /**
     * Helper function to delay execution (sleep).
     */
    private delay;
    /**
     * Generates content using the Gemini API with retry logic for rate limits and overloaded errors.
     */
    generateContent(systemPrompt: string, userPrompt: string, maxRetries?: number): Promise<string>;
}
export declare const geminiService: GeminiService;
//# sourceMappingURL=gemini-service.d.ts.map