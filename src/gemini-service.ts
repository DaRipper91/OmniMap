import { GoogleGenerativeAI } from '@google/generative-ai';

/**
 * Service for interacting with Gemini API.
 */
export class GeminiService {
  private genAI: GoogleGenerativeAI;
  private model: any;

  constructor() {
    // Attempt to read the API key from Vite environment or Node environment
    const apiKey = typeof process !== 'undefined' && process.env?.GEMINI_API_KEY
      ? process.env.GEMINI_API_KEY
      : (import.meta as any).env?.VITE_GEMINI_API_KEY;

    if (!apiKey) {
      console.warn("Gemini API key is missing. Please set process.env.GEMINI_API_KEY or VITE_GEMINI_API_KEY.");
    }

    this.genAI = new GoogleGenerativeAI(apiKey || "");
    this.model = this.genAI.getGenerativeModel({ model: "gemini-pro" });
  }

  /**
   * Helper function to delay execution (sleep).
   */
  private async delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * Generates content using the Gemini API with retry logic for rate limits and overloaded errors.
   */
  public async generateContent(systemPrompt: string, userPrompt: string, maxRetries = 3): Promise<string> {
    const combinedPrompt = `${systemPrompt}\n\n${userPrompt}`;

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        const result = await this.model.generateContent(combinedPrompt);
        const response = await result.response;
        return response.text();
      } catch (error: any) {
        const status = error?.status || error?.response?.status;
        const isRetryable = status === 429 || status === 503 || error.message?.includes('429') || error.message?.includes('503');

        if (isRetryable && attempt < maxRetries) {
          const backoff = attempt * 2000; // Exponential or linear backoff
          console.warn(`Gemini API error (Status: ${status}). Retrying in ${backoff}ms... (Attempt ${attempt}/${maxRetries})`);
          await this.delay(backoff);
        } else {
          console.error("Gemini API Request failed:", error);
          throw error;
        }
      }
    }
    
    throw new Error("Failed to generate content from Gemini API after retries.");
  }
}

export const geminiService = new GeminiService();
