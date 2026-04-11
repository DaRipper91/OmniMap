import {} from './types';
import { getChildren } from './graph-utils';
/**
 * Simple Tree Layout Engine for Project Singularity.
 * Positions nodes in a horizontal hierarchy.
 */
export const calculateTreeLayout = (state, rootNodeId) => {
    const positions = {};
    const xOffset = 300;
    const yOffset = 150;
    const traverse = (nodeId, x, yStart) => {
        const children = getChildren(state, nodeId);
        positions[nodeId] = { x, y: yStart };
        let currentY = yStart;
        children.forEach((child, index) => {
            // Recurse for children, incrementing X and adjusting Y
            currentY = traverse(child.id, x + xOffset, currentY);
            if (index < children.length - 1)
                currentY += yOffset;
        });
        return children.length > 0 ? currentY : yStart;
    };
    if (rootNodeId) {
        traverse(rootNodeId, 100, 100);
    }
    else {
        // Global layout: find all projects and lay them out vertically
        const projects = state.nodes.filter(n => n.type === 'PROJECT');
        let currentY = 100;
        projects.forEach(p => {
            currentY = traverse(p.id, 100, currentY) + yOffset * 2;
        });
    }
    return positions;
};
//# sourceMappingURL=layout-engine.js.map