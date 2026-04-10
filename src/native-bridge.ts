import { type Node } from './types';

/**
 * Singularity Native Bridge Interfaces
 * Provides a unified abstraction over Web and Capacitor plugins.
 */

export interface NativeFS {
  /**
   * Writes a graph node to a local markdown file.
   */
  writeNode: (node: Node, projectPath: string) => Promise<void>;
  
  /**
   * Reads and parses a markdown file into a graph node.
   */
  readNode: (filePath: string) => Promise<Node>;
  
  /**
   * Scans a directory for project files (.continuum or README.md).
   */
  scanDir: (path: string) => Promise<string[]>;
  
  /**
   * Requests native filesystem permissions.
   */
  requestPermissions: () => Promise<boolean>;
}

export interface NativeHTTP {
  /**
   * Performs a large file download with native progress tracking.
   */
  downloadFile: (url: string, targetPath: string, onProgress: (p: number) => void) => Promise<void>;
}

export type Platform = 'web' | 'android' | 'ios' | 'linux';

export interface BridgeConfig {
  platform: Platform;
  vaultRoot: string; // Base path for the Continuum vault
}
