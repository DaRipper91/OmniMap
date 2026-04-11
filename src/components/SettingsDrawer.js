import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Server, Trash2, ShieldAlert, Check } from 'lucide-react';
import { useContinuumStore } from '../store';
export const SettingsDrawer = ({ isOpen, onClose }) => {
    const runtimes = useContinuumStore(state => state.runtimes);
    const updateRuntime = useContinuumStore(state => state.updateRuntime);
    const resetStore = useContinuumStore(state => state.resetStore);
    const checkModelHealth = useContinuumStore(state => state.checkModelHealth);
    const handleUrlChange = (id, url) => {
        updateRuntime(id, { baseUrl: url, status: 'unknown' });
    };
    const handleFactoryReset = () => {
        if (window.confirm("CRITICAL WARNING: This will delete all projects, tasks, and notes stored locally. This action cannot be undone. Proceed?")) {
            resetStore();
            window.location.reload();
        }
    };
    return (_jsx(AnimatePresence, { children: isOpen && (_jsxs(_Fragment, { children: [_jsx(motion.div, { className: "drawer-overlay", initial: { opacity: 0 }, animate: { opacity: 1 }, exit: { opacity: 0 }, onClick: onClose, style: { zIndex: 1000 } }), _jsxs(motion.div, { className: "settings-drawer", initial: { y: "100%" }, animate: { y: 0 }, exit: { y: "100%" }, transition: { type: "spring", damping: 25, stiffness: 300 }, children: [_jsxs("div", { className: "settings-header", children: [_jsx("h3", { children: "Settings" }), _jsx("button", { className: "icon-btn close-btn", onClick: onClose, children: _jsx(X, { size: 24 }) })] }), _jsxs("div", { className: "settings-content", children: [_jsxs("div", { className: "settings-section", children: [_jsxs("div", { className: "section-title", children: [_jsx(Server, { size: 18 }), _jsx("h4", { children: "AI Runtime Configuration" })] }), _jsx("p", { className: "section-desc", children: "Configure the endpoints for your local AI models (e.g., LM Studio, GPT4All, Ollama)." }), runtimes.map(runtime => (_jsxs("div", { className: "runtime-config-card", children: [_jsxs("div", { className: "runtime-header", children: [_jsx("strong", { children: runtime.name }), _jsx("span", { className: `status-badge ${runtime.status}`, children: runtime.status.toUpperCase() })] }), _jsxs("div", { className: "input-group", children: [_jsx("label", { children: "API Base URL" }), _jsxs("div", { className: "url-input-row", children: [_jsx("input", { type: "text", value: runtime.baseUrl, onChange: (e) => handleUrlChange(runtime.id, e.target.value), placeholder: "http://127.0.0.1:4891" }), _jsx("button", { className: "test-btn", onClick: () => checkModelHealth(runtime.id), title: "Test Connection", children: _jsx(Check, { size: 18 }) })] })] })] }, runtime.id)))] }), _jsxs("div", { className: "settings-section danger-zone", children: [_jsxs("div", { className: "section-title danger-text", children: [_jsx(ShieldAlert, { size: 18 }), _jsx("h4", { children: "Danger Zone" })] }), _jsx("p", { className: "section-desc", children: "Destructive actions that cannot be undone." }), _jsxs("button", { className: "danger-btn", onClick: handleFactoryReset, children: [_jsx(Trash2, { size: 18 }), "Factory Reset (Delete All Data)"] })] })] })] }), _jsx("style", { children: `
            .settings-drawer {
              position: fixed;
              bottom: 0;
              left: 0;
              right: 0;
              height: 85vh;
              background: var(--surface);
              border-top-left-radius: var(--radius-lg);
              border-top-right-radius: var(--radius-lg);
              z-index: 1001;
              display: flex;
              flex-direction: column;
              box-shadow: 0 -10px 40px rgba(0,0,0,0.5);
              border-top: 1px solid var(--surface-variant);
            }

            .settings-header {
              display: flex;
              align-items: center;
              justify-content: space-between;
              padding: 20px 24px;
              border-bottom: 1px solid var(--surface-variant);
            }

            .settings-header h3 {
              margin: 0;
              font-size: 1.3rem;
              font-weight: 500;
            }

            .close-btn {
              background: var(--surface-variant);
              color: var(--on-surface);
            }

            .settings-content {
              flex: 1;
              overflow-y: auto;
              padding: 24px;
              display: flex;
              flex-direction: column;
              gap: 32px;
            }

            .settings-section {
              display: flex;
              flex-direction: column;
              gap: 16px;
            }

            .section-title {
              display: flex;
              align-items: center;
              gap: 8px;
              color: var(--primary);
            }

            .section-title h4 {
              margin: 0;
              font-size: 1.1rem;
            }

            .section-desc {
              margin: 0;
              font-size: 0.9rem;
              color: #A09E9F;
              line-height: 1.4;
            }

            .runtime-config-card {
              background: var(--secondary-container);
              padding: 16px;
              border-radius: var(--radius-md);
              display: flex;
              flex-direction: column;
              gap: 12px;
            }

            .runtime-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
            }

            .status-badge {
              font-size: 0.7rem;
              font-weight: bold;
              padding: 4px 8px;
              border-radius: 12px;
            }
            .status-badge.online { background: #B4E391; color: #000; }
            .status-badge.offline { background: #FF6464; color: #000; }
            .status-badge.unknown { background: #666; color: #FFF; }

            .input-group {
              display: flex;
              flex-direction: column;
              gap: 8px;
            }

            .input-group label {
              font-size: 0.85rem;
              color: #A09E9F;
            }

            .url-input-row {
              display: flex;
              gap: 8px;
            }

            .url-input-row input {
              flex: 1;
              background: var(--surface-variant);
              border: 1px solid transparent;
              color: var(--on-surface);
              padding: 12px;
              border-radius: 8px;
              font-size: 1rem;
              outline: none;
            }
            
            .url-input-row input:focus {
              border-color: var(--primary);
            }

            .test-btn {
              background: var(--primary-container);
              color: var(--primary);
              border: none;
              border-radius: 8px;
              padding: 0 16px;
              display: flex;
              align-items: center;
              justify-content: center;
              cursor: pointer;
            }

            .danger-zone {
              margin-top: auto;
              padding-top: 32px;
              border-top: 1px solid var(--surface-variant);
            }

            .danger-text {
              color: #FF6464;
            }

            .danger-btn {
              background: rgba(255, 100, 100, 0.1);
              color: #FF6464;
              border: 1px solid rgba(255, 100, 100, 0.3);
              padding: 16px;
              border-radius: var(--radius-md);
              font-size: 1rem;
              font-weight: 600;
              display: flex;
              align-items: center;
              justify-content: center;
              gap: 8px;
              cursor: pointer;
              transition: all 0.2s;
            }

            .danger-btn:hover {
              background: rgba(255, 100, 100, 0.2);
            }
          ` })] })) }));
};
//# sourceMappingURL=SettingsDrawer.js.map