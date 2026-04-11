import { type Node, type ContinuumState } from './types';
/**
 * Graph Traversal Utilities for Project Singularity
 */
/**
 * Resolves all child nodes for a given parent node based on PARENT_OF edges.
 */
export declare const getChildren: (state: ContinuumState, nodeId: string) => Node[];
/**
 * Resolves all parent nodes for a given child node.
 */
export declare const getParents: (state: ContinuumState, nodeId: string) => Node[];
/**
 * Resolves related nodes (bidirectional RELATED_TO edges).
 */
export declare const getRelatedNodes: (state: ContinuumState, nodeId: string) => Node[];
/**
 * Validates if adding an edge would create a circular dependency in a tree structure.
 */
export declare const wouldBeCircular: (state: ContinuumState, sourceId: string, targetId: string) => boolean;
//# sourceMappingURL=graph-utils.d.ts.map