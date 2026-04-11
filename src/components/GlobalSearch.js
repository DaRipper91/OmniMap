import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Search, FileText, CheckSquare, Folder } from 'lucide-react';
import { useContinuumStore } from '../store';
import { getPlatform } from '../native-bridge-impl';
import { Haptics, ImpactStyle } from '@capacitor/haptics';
export const GlobalSearch = ({ isOpen, onClose }) => {
    const [query, setQuery] = useState('');
    const projects = useContinuumStore(state => state.projects);
    const tasks = useContinuumStore(state => state.tasks);
    const notes = useContinuumStore(state => state.notes);
    const setActiveProject = useContinuumStore(state => state.setActiveProject);
    const triggerHaptic = () => {
        if (getPlatform() === 'android')
            Haptics.impact({ style: ImpactStyle.Light });
    };
    const results = [
        ...projects.filter(p => p.name.toLowerCase().includes(query.toLowerCase())).map(p => ({ type: 'project', item: p })),
        ...tasks.filter(t => t.title.toLowerCase().includes(query.toLowerCase()) || t.description.toLowerCase().includes(query.toLowerCase())).map(t => ({ type: 'task', item: t })),
        ...notes.filter(n => n.content.toLowerCase().includes(query.toLowerCase())).map(n => ({ type: 'note', item: n }))
    ].slice(0, 15);
    const handleSelect = (result) => {
        triggerHaptic();
        if (result.type === 'project') {
            setActiveProject(result.item.id);
        }
        else if (result.item.projectId) {
            setActiveProject(result.item.projectId);
        }
        onClose();
    };
    return (_jsx(AnimatePresence, { children: isOpen && (_jsxs("div", { className: "search-overlay", children: [_jsxs(motion.div, { className: "search-modal", initial: { opacity: 0, scale: 0.95, y: -20 }, animate: { opacity: 1, scale: 1, y: 0 }, exit: { opacity: 0, scale: 0.95, y: -20 }, children: [_jsxs("div", { className: "search-input-wrapper", children: [_jsx(Search, { size: 20, color: "var(--primary)" }), _jsx("input", { autoFocus: true, type: "text", placeholder: "Search projects, tasks, notes...", value: query, onChange: e => setQuery(e.target.value) }), _jsx("button", { className: "icon-btn", onClick: onClose, children: _jsx(X, { size: 20 }) })] }), _jsxs("div", { className: "search-results", children: [query && results.length === 0 && (_jsxs("div", { className: "empty-results", children: ["No results found for \"", query, "\""] })), query && results.map((res, i) => (_jsxs("div", { className: "search-result-item", onClick: () => handleSelect(res), children: [_jsxs("div", { className: "result-icon", children: [res.type === 'project' && _jsx(Folder, { size: 16 }), res.type === 'task' && _jsx(CheckSquare, { size: 16 }), res.type === 'note' && _jsx(FileText, { size: 16 })] }), _jsxs("div", { className: "result-content", children: [_jsx("h4", { children: res.type === 'project' ? res.item.name : (res.type === 'task' ? res.item.title : 'Note') }), _jsx("p", { children: res.type === 'project' ? res.item.description : (res.type === 'task' ? res.item.description : res.item.content) })] })] }, i)))] })] }), _jsx("style", { children: `
            .search-overlay {
              position: fixed;
              inset: 0;
              background: rgba(0,0,0,0.6);
              backdrop-filter: blur(4px);
              z-index: 2000;
              display: flex;
              justify-content: center;
              padding: 24px 16px;
            }
            .search-modal {
              background: var(--surface);
              width: 100%;
              max-width: 600px;
              max-height: 80vh;
              border-radius: var(--radius-lg);
              display: flex;
              flex-direction: column;
              overflow: hidden;
              box-shadow: 0 10px 40px rgba(0,0,0,0.5);
              border: 1px solid var(--surface-variant);
            }
            .search-input-wrapper {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 16px 20px;
              border-bottom: 1px solid var(--surface-variant);
              background: var(--secondary-container);
            }
            .search-input-wrapper input {
              flex: 1;
              background: transparent;
              border: none;
              color: var(--on-surface);
              font-size: 1.1rem;
              outline: none;
            }
            .search-results {
              flex: 1;
              overflow-y: auto;
              padding: 12px;
              display: flex;
              flex-direction: column;
              gap: 8px;
            }
            .search-result-item {
              display: flex;
              align-items: center;
              gap: 16px;
              padding: 12px 16px;
              background: var(--surface-variant);
              border-radius: var(--radius-md);
              cursor: pointer;
            }
            .search-result-item:active {
              background: var(--primary-container);
            }
            .result-icon {
              background: rgba(255,255,255,0.1);
              padding: 8px;
              border-radius: 50%;
              display: flex;
              color: var(--primary);
            }
            .result-content h4 {
              margin: 0 0 4px 0;
              font-size: 1rem;
            }
            .result-content p {
              margin: 0;
              font-size: 0.85rem;
              color: #A09E9F;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              max-width: 250px;
            }
            .empty-results {
              text-align: center;
              padding: 32px;
              color: #A09E9F;
            }
          ` })] })) }));
};
//# sourceMappingURL=GlobalSearch.js.map