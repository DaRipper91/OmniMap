import {} from './types';
/**
 * Graph Traversal Utilities for Project Singularity
 */
/**
 * Resolves all child nodes for a given parent node based on PARENT_OF edges.
 */
export const getChildren = (state, nodeId) => {
    const childIds = state.edges
        .filter(edge => edge.sourceId === nodeId && edge.type === 'PARENT_OF')
        .map(edge => edge.targetId);
    return state.nodes.filter(node => childIds.includes(node.id));
};
/**
 * Resolves all parent nodes for a given child node.
 */
export const getParents = (state, nodeId) => {
    const parentIds = state.edges
        .filter(edge => edge.targetId === nodeId && edge.type === 'PARENT_OF')
        .map(edge => edge.sourceId);
    return state.nodes.filter(node => parentIds.includes(node.id));
};
/**
 * Resolves related nodes (bidirectional RELATED_TO edges).
 */
export const getRelatedNodes = (state, nodeId) => {
    const relatedIds = state.edges
        .filter(edge => edge.type === 'RELATED_TO' && (edge.sourceId === nodeId || edge.targetId === nodeId))
        .map(edge => edge.sourceId === nodeId ? edge.targetId : edge.sourceId);
    return state.nodes.filter(node => relatedIds.includes(node.id));
};
/**
 * Validates if adding an edge would create a circular dependency in a tree structure.
 */
export const wouldBeCircular = (state, sourceId, targetId) => {
    if (sourceId === targetId)
        return true;
    const parents = getParents(state, sourceId);
    for (const parent of parents) {
        if (parent.id === targetId)
            return true;
        if (wouldBeCircular(state, parent.id, targetId))
            return true;
    }
    return false;
};
//# sourceMappingURL=graph-utils.js.map