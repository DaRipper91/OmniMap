import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import React from 'react';
import { useContinuumStore } from '../store';
import { ArrowRightLeft, Trash2 } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { getPlatform } from '../native-bridge-impl';
import { Haptics, ImpactStyle } from '@capacitor/haptics';
export const KanbanBoard = ({ tasks }) => {
    const proposeMutation = useContinuumStore(state => state.proposeMutation);
    const deleteTask = useContinuumStore(state => state.deleteTask);
    const triggerHaptic = () => {
        if (getPlatform() === 'android')
            Haptics.impact({ style: ImpactStyle.Medium });
    };
    const moveTask = (task, newStatus) => {
        triggerHaptic();
        proposeMutation({
            type: 'UPDATE',
            entity: 'TASK',
            payload: { id: task.id, data: { ...task.data, status: newStatus } },
        });
    };
    const columns = [
        { id: 'todo', title: 'To Do', color: '#A09E9F' },
        { id: 'in_progress', title: 'In Progress', color: '#7FBCFF' },
        { id: 'blocked', title: 'Blocked', color: '#FF6464' },
        { id: 'done', title: 'Done', color: '#B4E391' }
    ];
    return (_jsxs("div", { className: "kanban-scroll-container", children: [_jsx("div", { className: "kanban-board", children: columns.map(col => (_jsxs("div", { className: "kanban-column", children: [_jsxs("div", { className: "kanban-header", children: [_jsx("div", { className: "kanban-dot", style: { background: col.color } }), _jsx("h3", { children: col.title }), _jsx("span", { className: "kanban-count", children: tasks.filter(t => (t.data?.status || 'todo') === col.id).length })] }), _jsx("div", { className: "kanban-task-list", children: tasks.filter(t => (t.data?.status || 'todo') === col.id).map(task => (_jsxs("div", { className: "kanban-card", children: [_jsxs("div", { className: "kanban-card-header", children: [_jsx("h4", { children: task.title }), _jsx("button", { className: "icon-btn-small", onClick: () => deleteTask(task.id), children: _jsx(Trash2, { size: 14, color: "#FF6464" }) })] }), _jsx("div", { className: "kanban-markdown", children: _jsx(ReactMarkdown, { remarkPlugins: [remarkGfm], children: task.description }) }), _jsxs("div", { className: "kanban-actions", children: [col.id !== 'todo' && _jsx("button", { onClick: () => moveTask(task, 'todo'), children: "Todo" }), col.id !== 'in_progress' && _jsx("button", { onClick: () => moveTask(task, 'in_progress'), children: "Doing" }), col.id !== 'blocked' && _jsx("button", { onClick: () => moveTask(task, 'blocked'), children: "Block" }), col.id !== 'done' && _jsx("button", { onClick: () => moveTask(task, 'done'), children: "Done" })] })] }, task.id))) })] }, col.id))) }), _jsx("style", { children: `
        .kanban-scroll-container {
          width: 100%;
          overflow-x: auto;
          padding-bottom: 16px;
        }
        .kanban-board {
          display: flex;
          gap: 16px;
          padding: 8px;
          min-width: max-content;
        }
        .kanban-column {
          width: 300px;
          background: var(--secondary-container);
          border-radius: var(--radius-lg);
          padding: 16px;
          display: flex;
          flex-direction: column;
          gap: 16px;
          max-height: 70vh;
        }
        .kanban-header {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .kanban-dot {
          width: 12px;
          height: 12px;
          border-radius: 50%;
        }
        .kanban-header h3 {
          margin: 0;
          font-size: 1.1rem;
          flex: 1;
        }
        .kanban-count {
          background: rgba(255,255,255,0.1);
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 0.8rem;
          font-weight: bold;
        }
        .kanban-task-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
          overflow-y: auto;
        }
        .kanban-card {
          background: var(--surface-variant);
          padding: 16px;
          border-radius: var(--radius-md);
          display: flex;
          flex-direction: column;
          gap: 8px;
        }
        .kanban-card-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
        }
        .kanban-card-header h4 {
          margin: 0;
          font-size: 1rem;
          line-height: 1.3;
        }
        .icon-btn-small {
          background: transparent;
          border: none;
          cursor: pointer;
          opacity: 0.5;
        }
        .icon-btn-small:hover { opacity: 1; }
        .kanban-markdown {
          font-size: 0.85rem;
          color: #E6E1E5;
          opacity: 0.8;
          overflow: hidden;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
        }
        .kanban-markdown p { margin: 0 0 4px 0; }
        .kanban-actions {
          display: flex;
          gap: 8px;
          margin-top: 8px;
          flex-wrap: wrap;
        }
        .kanban-actions button {
          background: var(--surface);
          border: 1px solid var(--surface-variant);
          color: var(--primary);
          padding: 4px 8px;
          border-radius: 8px;
          font-size: 0.75rem;
          cursor: pointer;
        }
        .kanban-actions button:active { background: var(--primary-container); }
      ` })] }));
};
//# sourceMappingURL=KanbanBoard.js.map