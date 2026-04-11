import { jsx as _jsx, jsxs as _jsxs, Fragment as _Fragment } from "react/jsx-runtime";
import React from 'react';
import { useContinuumStore } from './store';
import { getChildren } from './graph-utils';
import { nativeFS, getPlatform } from './native-bridge-impl';
import { MindMap } from './components/MindMap';
import { SetupWizard } from './components/SetupWizard';
import { SettingsDrawer } from './components/SettingsDrawer';
import { GlobalSearch } from './components/GlobalSearch';
import { KanbanBoard } from './components/KanbanBoard';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { LayoutDashboard, Share2, BrainCircuit, Search, Settings, Menu, Plus, Undo2, CheckCircle2, Clock, Archive, ArrowRightLeft, Server, Activity, AlertCircle, Trash2, KanbanSquare, Cloud, List } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Haptics, ImpactStyle, NotificationType } from '@capacitor/haptics';
import { NexiumPortalMonitor } from './components/NexiumPortalMonitor';
function App() {
    const nodes = useContinuumStore((state) => state.nodes);
    const edges = useContinuumStore((state) => state.edges);
    const models = useContinuumStore((state) => state.models);
    const runtimes = useContinuumStore((state) => state.runtimes);
    const downloadProgress = useContinuumStore((state) => state.downloadProgress);
    const requestAI = useContinuumStore((state) => state.requestAI);
    const downloadModel = useContinuumStore((state) => state.downloadModel);
    const checkModelHealth = useContinuumStore((state) => state.checkModelHealth);
    const suggestedActions = useContinuumStore((state) => state.suggestedActions);
    const undoHistory = useContinuumStore((state) => state.undoHistory);
    const activeProjectId = useContinuumStore((state) => state.activeProjectId);
    const setActiveProject = useContinuumStore((state) => state.setActiveProject);
    const proposeMutation = useContinuumStore((state) => state.proposeMutation);
    const commitMutation = useContinuumStore((state) => state.commitMutation);
    const addProject = useContinuumStore((state) => state.addProject);
    const addNote = useContinuumStore((state) => state.addNote);
    const deleteTask = useContinuumStore((state) => state.deleteTask);
    const deleteNote = useContinuumStore((state) => state.deleteNote);
    const acceptAction = useContinuumStore((state) => state.acceptAction);
    const snoozeAction = useContinuumStore((state) => state.snoozeAction);
    const archiveAction = useContinuumStore((state) => state.archiveAction);
    const undo = useContinuumStore((state) => state.undo);
    const pendingMutations = useContinuumStore((state) => state.pendingMutations);
    const isThinking = useContinuumStore((state) => state.isThinking);
    const aiStatus = useContinuumStore((state) => state.aiStatus);
    const aiError = useContinuumStore((state) => state.aiError);
    const hasCompletedSetup = useContinuumStore((state) => state.hasCompletedSetup);
    const portals = useContinuumStore((state) => state.portals);
    const allProjects = useContinuumStore((state) => state.projects);
    const openPortal = useContinuumStore((state) => state.openPortal);
    const generateBriefing = useContinuumStore((state) => state.generateBriefing);
    const [activeTab, setActiveTab] = React.useState('dashboard');
    const [viewMode, setViewMode] = React.useState('list');
    const [isProjectDrawerOpen, setIsProjectDrawerOpen] = React.useState(false);
    const [isSettingsOpen, setIsSettingsOpen] = React.useState(false);
    const [isSearchOpen, setIsSearchOpen] = React.useState(false);
    const [platform] = React.useState(getPlatform());
    // Trigger haptic feedback
    const triggerHaptic = (style = ImpactStyle.Light) => {
        if (platform === 'android') {
            Haptics.impact({ style });
        }
    };
    const triggerSuccessHaptic = () => {
        if (platform === 'android') {
            Haptics.notification({ type: NotificationType.Success });
        }
    };
    React.useEffect(() => {
        if (platform === 'android') {
            nativeFS.requestPermissions();
        }
    }, [platform]);
    // Materialized Views
    const projects = nodes.filter(n => n.type === 'PROJECT');
    const activeProject = nodes.find(p => p.id === activeProjectId && p.type === 'PROJECT');
    const filteredTasks = activeProjectId
        ? getChildren({ nodes, edges }, activeProjectId).filter(n => n.type === 'TASK')
        : nodes.filter(n => n.type === 'TASK');
    const filteredNotes = activeProjectId
        ? getChildren({ nodes, edges }, activeProjectId).filter(n => n.type === 'IDEA')
        : nodes.filter(n => n.type === 'IDEA');
    const filteredActions = activeProjectId
        ? suggestedActions.filter(a => a.projectId === activeProjectId && a.status === 'active')
        : suggestedActions.filter(a => a.status === 'active');
    const handleToggleStatus = (task) => {
        triggerHaptic(ImpactStyle.Medium);
        const currentStatus = task.data?.status || 'todo';
        const nextStatus = currentStatus === 'todo' ? 'in_progress' : (currentStatus === 'in_progress' ? 'done' : 'todo');
        if (nextStatus === 'done')
            triggerSuccessHaptic();
        proposeMutation({
            type: 'UPDATE',
            entity: 'TASK',
            payload: { id: task.id, data: { ...task.data, status: nextStatus } },
        });
    };
    const handleOpenPortal = async () => {
        triggerHaptic();
        const briefing = generateBriefing();
        const activeProjectData = allProjects.find(p => p.id === activeProjectId);
        const repo = activeProjectData?.path || 'DaRipper91/contiinuum';
        await openPortal(repo, briefing);
    };
    const handleQuickCapture = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.currentTarget);
        const content = formData.get('note-content');
        if (!content.trim())
            return;
        triggerSuccessHaptic();
        addNote({
            id: Math.random().toString(36).substring(2, 9),
            projectId: activeProjectId || undefined,
            content,
            createdAt: Date.now(),
            updatedAt: Date.now(),
            tags: ['quick-capture'],
        });
        e.currentTarget.reset();
        // Simulate AI parsing context from quick capture
        if (!isThinking) {
            await requestAI('jules', 'task-generator', { note: content, projectId: activeProjectId });
        }
    };
    return (_jsxs("div", { className: "app-container native-shell", children: [!hasCompletedSetup && _jsx(SetupWizard, {}), _jsx(SettingsDrawer, { isOpen: isSettingsOpen, onClose: () => setIsSettingsOpen(false) }), _jsxs("header", { className: "native-app-bar", children: [_jsx("button", { className: "icon-btn", onClick: () => { setIsProjectDrawerOpen(true); triggerHaptic(); }, children: _jsx(Menu, { size: 24 }) }), _jsxs("div", { className: "app-bar-title", style: { display: 'flex', alignItems: 'center', gap: '8px' }, children: [activeProject ? activeProject.title : 'Continuum', _jsx(AnimatePresence, { children: isThinking && (_jsx(motion.div, { initial: { opacity: 0, scale: 0.8 }, animate: { opacity: 1, scale: [1, 1.2, 1] }, exit: { opacity: 0, scale: 0.8 }, transition: { repeat: Infinity, duration: 1.5 }, className: "thinking-indicator", children: _jsx(BrainCircuit, { size: 16, color: "var(--primary)" }) })) })] }), _jsxs("div", { className: "app-bar-actions", children: [_jsx("button", { className: "icon-btn", onClick: () => triggerHaptic(), children: _jsx(Search, { size: 22 }) }), _jsx("button", { className: "icon-btn", onClick: () => { setIsSettingsOpen(true); triggerHaptic(); }, children: _jsx(Settings, { size: 22 }) })] })] }), _jsx("main", { className: "native-content", children: _jsxs(AnimatePresence, { mode: "wait", children: [activeTab === 'dashboard' && (_jsxs(motion.section, { initial: { opacity: 0, y: 20 }, animate: { opacity: 1, y: 0 }, exit: { opacity: 0, y: -20 }, className: "dashboard-view", children: [pendingMutations.length > 0 && (_jsxs("div", { className: "native-card primary-tonal", children: [_jsxs("div", { className: "card-header", style: { display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }, children: [_jsx("h3", { style: { margin: 0 }, children: "Approvals Required" }), _jsx("span", { className: "badge", style: { background: 'var(--primary)', color: '#000', padding: '2px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold' }, children: pendingMutations.length })] }), _jsx("div", { className: "mutation-stack", style: { display: 'flex', flexDirection: 'column', gap: '8px' }, children: pendingMutations.map(m => (_jsxs("div", { className: "mutation-row", style: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(0,0,0,0.2)', padding: '12px', borderRadius: '8px' }, children: [_jsxs("span", { style: { fontSize: '0.9rem' }, children: [m.type, " ", m.entity] }), _jsx("button", { className: "chip-btn", style: { background: 'var(--primary-container)', color: 'var(--primary)', border: 'none', padding: '6px 12px', borderRadius: '16px', fontWeight: 600 }, onClick: () => { commitMutation(m.id); triggerHaptic(); }, children: "Approve" })] }, m.id))) })] })), _jsx("div", { className: "capture-surface", children: _jsxs("form", { onSubmit: handleQuickCapture, style: { display: 'flex', width: '100%' }, children: [_jsx("textarea", { name: "note-content", placeholder: "Capture a thought...", rows: 1 }), _jsx("button", { type: "submit", className: "fab-mini", children: _jsx(Plus, { size: 20 }) })] }) }), (isThinking || aiError) && (_jsxs("div", { className: `ai-status-message ${aiStatus}`, children: [aiStatus === 'thinking' && _jsx(motion.div, { animate: { rotate: 360 }, transition: { duration: 2, repeat: Infinity, ease: "linear" }, style: { display: 'inline-block', marginRight: '8px' }, children: "\u2728" }), aiStatus === 'retrying' && _jsx("span", { style: { marginRight: '8px' }, children: "\u23F3" }), aiStatus === 'error' && _jsx("span", { style: { marginRight: '8px' }, children: "\u26A0\uFE0F" }), aiError || (aiStatus === 'thinking' ? 'Jules is thinking...' : 'Opening Portal...')] })), _jsxs("div", { className: "native-section", children: [_jsxs("div", { className: "section-title", style: { marginBottom: '16px', fontWeight: 'bold', color: 'var(--primary)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }, children: [_jsxs("span", { children: ["Tasks ", _jsx("span", { style: { opacity: 0.6, fontSize: '0.9em' }, children: filteredTasks.length })] }), _jsxs("button", { onClick: handleOpenPortal, disabled: isThinking, style: {
                                                        background: 'var(--primary-container)',
                                                        color: 'var(--primary)',
                                                        border: 'none',
                                                        borderRadius: '12px',
                                                        padding: '6px 12px',
                                                        fontSize: '0.8rem',
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        gap: '6px',
                                                        fontWeight: 'bold'
                                                    }, children: [_jsx(Cloud, { size: 14 }), " Open Portal"] })] }), filteredTasks.length === 0 ? (_jsxs("div", { className: "empty-state", children: [_jsx(CheckCircle2, { size: 40, opacity: 0.3 }), _jsx("p", { children: "No tasks here. Use the quick capture above to let AI generate some!" })] })) : (_jsx("div", { className: "native-task-list", children: filteredTasks.map(task => (_jsxs(motion.div, { whileTap: { scale: 0.98 }, className: `native-task-card status-${task.data?.status || 'todo'}`, onClick: () => handleToggleStatus(task), children: [_jsx("div", { className: "task-indicator" }), _jsxs("div", { className: "task-content", style: { flex: 1 }, children: [_jsx("h4", { style: { margin: '0 0 4px 0' }, children: task.title }), _jsx("p", { style: { margin: 0, fontSize: '0.85rem', opacity: 0.7 }, children: task.description })] }), _jsx(ArrowRightLeft, { size: 16, className: "task-icon", style: { opacity: 0.5 } })] }, task.id))) }))] }), _jsxs("div", { className: "native-section", style: { marginTop: '24px' }, children: [_jsx("div", { className: "section-title", style: { marginBottom: '16px', fontWeight: 'bold', color: 'var(--primary)' }, children: "Recent Notes" }), filteredNotes.length === 0 ? (_jsx("div", { className: "empty-state", children: _jsx("p", { children: "Your notes will appear here." }) })) : (_jsx("div", { className: "native-note-stack", style: { display: 'flex', flexDirection: 'column', gap: '12px' }, children: filteredNotes.map(note => (_jsx("div", { className: "native-note-card", style: { background: 'var(--surface-variant)', padding: '16px', borderRadius: 'var(--radius-md)' }, children: note.data?.content || note.description }, note.id))) }))] })] }, "dashboard")), activeTab === 'map' && (_jsx(motion.section, { initial: { opacity: 0 }, animate: { opacity: 1 }, exit: { opacity: 0 }, className: "map-view", children: _jsx(MindMap, {}) }, "map")), activeTab === 'models' && (_jsxs(motion.section, { initial: { opacity: 0, x: 50 }, animate: { opacity: 1, x: 0 }, exit: { opacity: 0, x: -50 }, className: "models-view", children: [_jsx("div", { className: "section-title", style: { marginBottom: '16px', fontWeight: 'bold', color: 'var(--primary)', fontSize: '1.2rem' }, children: "AI Runtimes & Intelligence" }), _jsxs("div", { className: "runtime-list", style: { display: 'flex', flexDirection: 'column', gap: '16px' }, children: [runtimes.map(runtime => (_jsxs("div", { className: "native-card", style: { background: 'var(--secondary-container)', borderRadius: 'var(--radius-lg)', padding: '20px' }, children: [_jsxs("div", { style: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }, children: [_jsxs("div", { style: { display: 'flex', alignItems: 'center', gap: '12px' }, children: [_jsx("div", { style: {
                                                                        width: '40px', height: '40px', borderRadius: '50%',
                                                                        background: runtime.status === 'online' ? 'rgba(180, 227, 145, 0.2)' : 'rgba(255, 100, 100, 0.2)',
                                                                        display: 'flex', alignItems: 'center', justifyContent: 'center'
                                                                    }, children: _jsx(Server, { size: 20, color: runtime.status === 'online' ? '#B4E391' : '#FF6464' }) }), _jsxs("div", { children: [_jsx("h3", { style: { margin: 0, fontSize: '1.1rem' }, children: runtime.name }), _jsx("p", { style: { margin: 0, fontSize: '0.85rem', opacity: 0.6 }, children: runtime.baseUrl })] })] }), _jsx("div", { style: {
                                                                padding: '4px 12px',
                                                                borderRadius: '12px',
                                                                fontSize: '0.8rem',
                                                                fontWeight: 'bold',
                                                                background: runtime.status === 'online' ? '#B4E391' : (runtime.status === 'offline' ? '#FF6464' : '#666'),
                                                                color: '#000'
                                                            }, children: runtime.status.toUpperCase() })] }), _jsxs("button", { onClick: () => { triggerHaptic(); checkModelHealth(runtime.id); }, style: {
                                                        width: '100%', padding: '12px', background: 'var(--surface-variant)',
                                                        color: 'var(--on-surface)', border: 'none', borderRadius: '12px',
                                                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
                                                        marginTop: '16px', fontWeight: 'bold'
                                                    }, children: [_jsx(Activity, { size: 16 }), " Check Health"] })] }, runtime.id))), _jsx(NexiumPortalMonitor, {}), _jsxs("div", { className: "empty-state", style: { marginTop: '24px', opacity: 0.7, textAlign: 'center' }, children: [_jsx(AlertCircle, { size: 32, style: { margin: '0 auto 12px' } }), _jsx("p", { style: { fontSize: '0.9rem', maxWidth: '280px', margin: '0 auto' }, children: "Continuum connects directly to your local models. Start a local server (like LM Studio or GPT4All) on port 4891 to enable AI features." })] })] })] }, "models"))] }) }), _jsx(AnimatePresence, { children: isProjectDrawerOpen && (_jsxs(_Fragment, { children: [_jsx(motion.div, { className: "drawer-overlay", initial: { opacity: 0 }, animate: { opacity: 1 }, exit: { opacity: 0 }, onClick: () => setIsProjectDrawerOpen(false) }), _jsxs(motion.div, { className: "project-drawer", initial: { y: "100%" }, animate: { y: 0 }, exit: { y: "100%" }, transition: { type: "spring", damping: 25, stiffness: 300 }, children: [_jsx("div", { className: "drawer-handle" }), _jsx("h3", { style: { margin: '0 0 16px 0', color: 'var(--on-surface)' }, children: "Select Context" }), _jsxs("div", { className: "drawer-list", children: [_jsx("button", { className: `drawer-item ${activeProjectId === null ? 'active' : ''}`, onClick: () => { setActiveProject(null); setIsProjectDrawerOpen(false); triggerHaptic(); }, children: "\uD83C\uDF0D Global Context" }), projects.map(p => (_jsxs("button", { className: `drawer-item ${activeProjectId === p.id ? 'active' : ''}`, onClick: () => { setActiveProject(p.id); setIsProjectDrawerOpen(false); triggerHaptic(); }, children: ["# ", p.title] }, p.id)))] }), _jsxs("button", { onClick: () => { setIsProjectDrawerOpen(false); triggerHaptic(); }, style: {
                                        width: '100%', padding: '16px', background: 'var(--surface-variant)',
                                        color: 'var(--on-surface)', border: 'none', borderRadius: '12px',
                                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
                                        marginTop: '16px', fontWeight: 'bold'
                                    }, children: [_jsx(Plus, { size: 20 }), " New Project"] })] })] })) }), _jsxs("nav", { className: "native-bottom-nav", children: [_jsxs("button", { className: `nav-btn ${activeTab === 'dashboard' ? 'active' : ''}`, onClick: () => { setActiveTab('dashboard'); triggerHaptic(); }, children: [_jsx(LayoutDashboard, { size: 24 }), _jsx("span", { children: "Feed" })] }), _jsxs("button", { className: `nav-btn ${activeTab === 'map' ? 'active' : ''}`, onClick: () => { setActiveTab('map'); triggerHaptic(); }, children: [_jsx(Share2, { size: 24 }), _jsx("span", { children: "Graph" })] }), _jsxs("button", { className: `nav-btn ${activeTab === 'models' ? 'active' : ''}`, onClick: () => { setActiveTab('models'); triggerHaptic(); }, children: [_jsx(BrainCircuit, { size: 24 }), _jsx("span", { children: "Intelligence" })] })] }), _jsx(AnimatePresence, { children: undoHistory.length > 0 && (_jsxs(motion.div, { initial: { y: 100, opacity: 0 }, animate: { y: -80, opacity: 1 }, exit: { y: 100, opacity: 0 }, className: "native-toast", children: [_jsx("span", { children: "Change committed" }), _jsxs("button", { onClick: () => { undo(); triggerHaptic(); }, style: { background: 'transparent', border: '1px solid var(--primary)', color: 'var(--primary)', padding: '6px 12px', borderRadius: '16px', display: 'flex', alignItems: 'center', gap: '4px' }, children: [_jsx(Undo2, { size: 16 }), " Undo"] })] })) }), _jsx("style", { children: `
        :root {
          --surface: #1C1B1F;
          --on-surface: #E6E1E5;
          --surface-variant: #49454F;
          --primary: #D0BCFF;
          --primary-container: #4F378B;
          --secondary-container: #332D41;
          --accent: #D0BCFF;
          --radius-lg: 28px;
          --radius-md: 16px;
        }

        body { 
          background: var(--surface); 
          color: var(--on-surface); 
          font-family: 'Roboto', system-ui, sans-serif;
          overscroll-behavior: none;
        }

        .native-shell {
          height: 100vh;
          display: flex;
          flex-direction: column;
          padding-bottom: 80px; /* Space for nav */
        }

        .native-app-bar {
          height: 64px;
          padding: 0 16px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          background: var(--surface);
          z-index: 10;
        }

        .app-bar-title { font-size: 1.4rem; font-weight: 400; }

        .native-content { flex: 1; overflow-y: auto; padding: 16px; }

        .native-bottom-nav {
          position: fixed;
          bottom: 0;
          left: 0;
          right: 0;
          height: 80px;
          background: #25232A;
          display: flex;
          justify-content: space-around;
          align-items: center;
          padding-bottom: env(safe-area-inset-bottom);
          box-shadow: 0 -2px 10px rgba(0,0,0,0.3);
          border-top: 1px solid var(--surface-variant);
          z-index: 50;
        }

        .nav-btn {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 4px;
          background: transparent;
          border: none;
          color: var(--on-surface);
          opacity: 0.7;
          width: 80px;
          cursor: pointer;
        }

        .nav-btn.active { opacity: 1; color: var(--primary); }
        .nav-btn.active span { font-weight: 700; }
        .nav-btn span { font-size: 0.75rem; }

        .empty-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 32px 16px;
          text-align: center;
          background: rgba(255,255,255,0.02);
          border: 1px dashed rgba(255,255,255,0.1);
          border-radius: var(--radius-lg);
          color: #A09E9F;
        }

        /* MD3 Cards */
        .native-card {
          background: var(--secondary-container);
          border-radius: var(--radius-lg);
          padding: 20px;
          margin-bottom: 16px;
        }

        .native-task-card {
          background: var(--surface-variant);
          border-radius: var(--radius-md);
          padding: 16px;
          margin-bottom: 12px;
          display: flex;
          align-items: center;
          gap: 16px;
          position: relative;
          overflow: hidden;
          cursor: pointer;
        }

        .task-indicator {
          width: 4px;
          height: 40px;
          background: var(--primary);
          border-radius: 2px;
        }

        .status-done .task-indicator { background: #B4E391; }
        .status-in_progress .task-indicator { background: #7FBCFF; }

        .capture-surface {
          background: var(--secondary-container);
          border-radius: 40px;
          padding: 8px 16px;
          display: flex;
          align-items: center;
          margin-bottom: 24px;
        }

        .capture-surface textarea {
          flex: 1;
          background: transparent;
          border: none;
          color: white;
          padding: 8px;
          font-size: 1rem;
          outline: none;
          resize: none;
        }

        .fab-mini {
          background: var(--primary-container);
          color: var(--primary);
          border: none;
          width: 40px;
          height: 40px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
        }

        /* Drawer */
        .drawer-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; }
        .project-drawer {
          position: fixed;
          bottom: 0;
          left: 0;
          right: 0;
          background: #25232A;
          border-top-left-radius: var(--radius-lg);
          border-top-right-radius: var(--radius-lg);
          padding: 24px;
          z-index: 101;
          max-height: 80vh;
          overflow-y: auto;
        }

        .drawer-handle {
          width: 40px;
          height: 5px;
          background: var(--surface-variant);
          border-radius: 10px;
          margin: 0 auto 20px;
          opacity: 0.5;
        }

        .drawer-item {
          display: block;
          width: 100%;
          text-align: left;
          padding: 16px;
          background: transparent;
          border: none;
          color: var(--on-surface);
          border-radius: var(--radius-md);
          font-size: 1.1rem;
          margin-bottom: 8px;
          cursor: pointer;
        }

        .drawer-item.active { background: var(--primary-container); color: var(--primary); }

        .native-toast {
          position: fixed;
          background: #25232A;
          border: 1px solid var(--primary);
          padding: 12px 24px;
          border-radius: 100px;
          left: 16px;
          right: 16px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          z-index: 1000;
        }

        .thinking-indicator {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          background: var(--primary-container);
          border-radius: 50%;
          width: 24px;
          height: 24px;
        }

        .icon-btn { background: transparent; border: none; color: var(--on-surface); padding: 8px; border-radius: 50%; cursor: pointer; }
        
        .ai-status-message {
          margin: 0 16px 24px 16px;
          padding: 12px 16px;
          border-radius: 12px;
          font-size: 0.9rem;
          display: flex;
          align-items: center;
          transition: all 0.3s ease;
        }

        .ai-status-message.thinking { background: rgba(208, 188, 255, 0.1); border: 1px solid rgba(208, 188, 255, 0.3); color: var(--primary); }
        .ai-status-message.retrying { background: rgba(255, 183, 77, 0.1); border: 1px solid rgba(255, 183, 77, 0.3); color: #ffb74d; }
        .ai-status-message.error { background: rgba(239, 68, 68, 0.1); border: 1px solid rgba(239, 68, 68, 0.3); color: #ef4444; }

        .thinking { animation: pulse 1.5s infinite; }
        @keyframes pulse {
          0% { opacity: 0.6; }
          50% { opacity: 1; }
          100% { opacity: 0.6; }
        }

        .native-shell * { transition: background-color 0.2s; }
      ` })] }));
}
export default App;
//# sourceMappingURL=App.js.map