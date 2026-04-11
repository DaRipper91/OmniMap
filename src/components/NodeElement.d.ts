import React from 'react';
import { type Node } from '../types';
interface NodeElementProps {
    node: Node;
    x: number;
    y: number;
    isSelected: boolean;
    onSelect: (id: string) => void;
}
export declare const NodeElement: React.FC<NodeElementProps>;
export {};
//# sourceMappingURL=NodeElement.d.ts.map