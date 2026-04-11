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
import { 
  LayoutDashboard, 
  Share2, 
  BrainCircuit, 
  Search, 
  Settings, 
  Menu, 
  Plus, 
  Undo2,
  CheckCircle2,
  Clock,
  Archive,
  ArrowRightLeft,
  Server,
  Activity,
  AlertCircle,
  Trash2,
  KanbanSquare,
  Cloud,
  List
} from 'lucide-react';
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

  const [activeTab, setActiveTab] = React.useState<'dashboard' | 'map' | 'models'>('dashboard');
  const [viewMode, setViewMode] = React.useState<'list' | 'kanban'>('list');
  const [isProjectDrawerOpen, setIsProjectDrawerOpen] = React.useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = React.useState(false);
  const [isSearchOpen, setIsSearchOpen] = React.useState(false);
  const [platform] = React.useState(getPlatform());

  // Trigger haptic feedback
  const triggerHaptic = (style: ImpactStyle = ImpactStyle.Light) => {
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
    ? getChildren({ nodes, edges } as any, activeProjectId).filter(n => n.type === 'TASK')
    : nodes.filter(n => n.type === 'TASK');
  const filteredNotes = activeProjectId
    ? getChildren({ nodes, edges } as any, activeProjectId).filter(n => n.type === 'IDEA')
    : nodes.filter(n => n.type === 'IDEA');
  const filteredActions = activeProjectId
    ? suggestedActions.filter(a => a.projectId === activeProjectId && a.status === 'active')
    : suggestedActions.filter(a => a.status === 'active');

  const handleToggleStatus = (task: any) => {
    triggerHaptic(ImpactStyle.Medium);
    const currentStatus = task.data?.status || 'todo';
    const nextStatus = currentStatus === 'todo' ? 'in_progress' : (currentStatus === 'in_progress' ? 'done' : 'todo');
    if (nextStatus === 'done') triggerSuccessHaptic();

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

  const handleQuickCapture = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const content = formData.get('note-content') as string;
    if (!content.trim()) return;

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

  return (
    <div className="app-container native-shell">
      {!hasCompletedSetup && <SetupWizard />}
      <SettingsDrawer isOpen={isSettingsOpen} onClose={() => setIsSettingsOpen(false)} />
      
      {/* Immersive Top Header */}
      <header className="native-app-bar">
        <button className="icon-btn" onClick={() => { setIsProjectDrawerOpen(true); triggerHaptic(); }}>
          <Menu size={24} />
        </button>
        <div className="app-bar-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {activeProject ? activeProject.title : 'Continuum'}
          <AnimatePresence>
            {isThinking && (
              <motion.div 
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: [1, 1.2, 1] }}
                exit={{ opacity: 0, scale: 0.8 }}
                transition={{ repeat: Infinity, duration: 1.5 }}
                className="thinking-indicator"
              >
                <BrainCircuit size={16} color="var(--primary)" />
              </motion.div>
            )}
          </AnimatePresence>
        </div>
        <div className="app-bar-actions">
          <button className="icon-btn" onClick={() => triggerHaptic()}><Search size={22} /></button>
          <button className="icon-btn" onClick={() => { setIsSettingsOpen(true); triggerHaptic(); }}><Settings size={22} /></button>
        </div>
      </header>

      <main className="native-content">
        <AnimatePresence mode="wait">
          {activeTab === 'dashboard' && (
            <motion.section 
              key="dashboard"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              className="dashboard-view"
            >
              {/* Approvals Section (MD3 Card) */}
              {pendingMutations.length > 0 && (
                <div className="native-card primary-tonal">
                  <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                    <h3 style={{ margin: 0 }}>Approvals Required</h3>
                    <span className="badge" style={{ background: 'var(--primary)', color: '#000', padding: '2px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold' }}>{pendingMutations.length}</span>
                  </div>
                  <div className="mutation-stack" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {pendingMutations.map(m => (
                      <div key={m.id} className="mutation-row" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(0,0,0,0.2)', padding: '12px', borderRadius: '8px' }}>
                        <span style={{ fontSize: '0.9rem' }}>{m.type} {m.entity}</span>
                        <button className="chip-btn" style={{ background: 'var(--primary-container)', color: 'var(--primary)', border: 'none', padding: '6px 12px', borderRadius: '16px', fontWeight: 600 }} onClick={() => { commitMutation(m.id); triggerHaptic(); }}>Approve</button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Quick Capture (MD3 Input) */}
              <div className="capture-surface">
                <form onSubmit={handleQuickCapture} style={{ display: 'flex', width: '100%' }}>
                  <textarea name="note-content" placeholder="Capture a thought..." rows={1} />
                  <button type="submit" className="fab-mini"><Plus size={20} /></button>
                </form>
              </div>

              {/* AI Status Message */}
              {(isThinking || aiError) && (
                <div className={`ai-status-message ${aiStatus}`}>
                  {aiStatus === 'thinking' && <motion.div animate={{ rotate: 360 }} transition={{ duration: 2, repeat: Infinity, ease: "linear" }} style={{ display: 'inline-block', marginRight: '8px' }}>✨</motion.div>}
                  {aiStatus === 'retrying' && <span style={{ marginRight: '8px' }}>⏳</span>}
                  {aiStatus === 'error' && <span style={{ marginRight: '8px' }}>⚠️</span>}
                  {aiError || (aiStatus === 'thinking' ? 'Jules is thinking...' : 'Opening Portal...')}
                </div>
              )}

              {/* Task Section */}
              <div className="native-section">
                <div className="section-title" style={{ marginBottom: '16px', fontWeight: 'bold', color: 'var(--primary)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>Tasks <span style={{ opacity: 0.6, fontSize: '0.9em' }}>{filteredTasks.length}</span></span>
                  <button 
                    onClick={handleOpenPortal}
                    disabled={isThinking}
                    style={{ 
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
                    }}
                  >
                    <Cloud size={14} /> Open Portal
                  </button>
                </div>
                
                {filteredTasks.length === 0 ? (
                  <div className="empty-state">
                    <CheckCircle2 size={40} opacity={0.3} />
                    <p>No tasks here. Use the quick capture above to let AI generate some!</p>
                  </div>
                ) : (
                  <div className="native-task-list">
                    {filteredTasks.map(task => (
                      <motion.div 
                        key={task.id} 
                        whileTap={{ scale: 0.98 }}
                        className={`native-task-card status-${task.data?.status || 'todo'}`}
                        onClick={() => handleToggleStatus(task)}
                      >
                        <div className="task-indicator"></div>
                        <div className="task-content" style={{ flex: 1 }}>
                          <h4 style={{ margin: '0 0 4px 0' }}>{task.title}</h4>
                          <p style={{ margin: 0, fontSize: '0.85rem', opacity: 0.7 }}>{task.description}</p>
                        </div>
                        <ArrowRightLeft size={16} className="task-icon" style={{ opacity: 0.5 }} />
                      </motion.div>
                    ))}
                  </div>
                )}
              </div>

              {/* Notes Feed */}
              <div className="native-section" style={{ marginTop: '24px' }}>
                <div className="section-title" style={{ marginBottom: '16px', fontWeight: 'bold', color: 'var(--primary)' }}>Recent Notes</div>
                {filteredNotes.length === 0 ? (
                  <div className="empty-state">
                    <p>Your notes will appear here.</p>
                  </div>
                ) : (
                  <div className="native-note-stack" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {filteredNotes.map(note => (
                      <div key={note.id} className="native-note-card" style={{ background: 'var(--surface-variant)', padding: '16px', borderRadius: 'var(--radius-md)' }}>
                        {note.data?.content || note.description}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </motion.section>
          )}

          {activeTab === 'map' && (
            <motion.section key="map" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="map-view">
              <MindMap />
            </motion.section>
          )}

          {activeTab === 'models' && (
            <motion.section key="models" initial={{ opacity: 0, x: 50 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -50 }} className="models-view">
              <div className="section-title" style={{ marginBottom: '16px', fontWeight: 'bold', color: 'var(--primary)', fontSize: '1.2rem' }}>
                AI Runtimes & Intelligence
              </div>
              
              <div className="runtime-list" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {runtimes.map(runtime => (
                  <div key={runtime.id} className="native-card" style={{ background: 'var(--secondary-container)', borderRadius: 'var(--radius-lg)', padding: '20px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div style={{ 
                          width: '40px', height: '40px', borderRadius: '50%', 
                          background: runtime.status === 'online' ? 'rgba(180, 227, 145, 0.2)' : 'rgba(255, 100, 100, 0.2)',
                          display: 'flex', alignItems: 'center', justifyContent: 'center'
                        }}>
                          <Server size={20} color={runtime.status === 'online' ? '#B4E391' : '#FF6464'} />
                        </div>
                        <div>
                          <h3 style={{ margin: 0, fontSize: '1.1rem' }}>{runtime.name}</h3>
                          <p style={{ margin: 0, fontSize: '0.85rem', opacity: 0.6 }}>{runtime.baseUrl}</p>
                        </div>
                      </div>
                      <div style={{ 
                        padding: '4px 12px', 
                        borderRadius: '12px', 
                        fontSize: '0.8rem', 
                        fontWeight: 'bold',
                        background: runtime.status === 'online' ? '#B4E391' : (runtime.status === 'offline' ? '#FF6464' : '#666'),
                        color: '#000'
                      }}>
                        {runtime.status.toUpperCase()}
                      </div>
                    </div>
                    
                    <button 
                      onClick={() => { triggerHaptic(); checkModelHealth(runtime.id); }}
                      style={{ 
                        width: '100%', padding: '12px', background: 'var(--surface-variant)', 
                        color: 'var(--on-surface)', border: 'none', borderRadius: '12px',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
                        marginTop: '16px', fontWeight: 'bold'
                      }}
                    >
                      <Activity size={16} /> Check Health
                    </button>
                  </div>
                ))}

                <NexiumPortalMonitor />

                <div className="empty-state" style={{ marginTop: '24px', opacity: 0.7, textAlign: 'center' }}>
                  <AlertCircle size={32} style={{ margin: '0 auto 12px' }} />
                  <p style={{ fontSize: '0.9rem', maxWidth: '280px', margin: '0 auto' }}>
                    Continuum connects directly to your local models. Start a local server (like LM Studio or GPT4All) on port 4891 to enable AI features.
                  </p>
                </div>
              </div>
            </motion.section>
          )}
        </AnimatePresence>
      </main>

      {/* Project Drawer (Overlay) */}
      <AnimatePresence>
        {isProjectDrawerOpen && (
          <>
            <motion.div 
              className="drawer-overlay"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsProjectDrawerOpen(false)}
            />
            <motion.div 
              className="project-drawer"
              initial={{ y: "100%" }}
              animate={{ y: 0 }}
              exit={{ y: "100%" }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
            >
              <div className="drawer-handle"></div>
              <h3 style={{ margin: '0 0 16px 0', color: 'var(--on-surface)' }}>Select Context</h3>
              <div className="drawer-list">
                <button 
                  className={`drawer-item ${activeProjectId === null ? 'active' : ''}`}
                  onClick={() => { setActiveProject(null); setIsProjectDrawerOpen(false); triggerHaptic(); }}
                >
                  🌍 Global Context
                </button>
                {projects.map(p => (
                  <button 
                    key={p.id}
                    className={`drawer-item ${activeProjectId === p.id ? 'active' : ''}`}
                    onClick={() => { setActiveProject(p.id); setIsProjectDrawerOpen(false); triggerHaptic(); }}
                  >
                    # {p.title}
                  </button>
                ))}
              </div>
              <button 
                onClick={() => { setIsProjectDrawerOpen(false); triggerHaptic(); }}
                style={{
                  width: '100%', padding: '16px', background: 'var(--surface-variant)', 
                  color: 'var(--on-surface)', border: 'none', borderRadius: '12px',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
                  marginTop: '16px', fontWeight: 'bold'
                }}
              >
                <Plus size={20} /> New Project
              </button>
            </motion.div>
          </>
        )}
      </AnimatePresence>

      {/* Persistent Bottom Navigation */}
      <nav className="native-bottom-nav">
        <button 
          className={`nav-btn ${activeTab === 'dashboard' ? 'active' : ''}`}
          onClick={() => { setActiveTab('dashboard'); triggerHaptic(); }}
        >
          <LayoutDashboard size={24} />
          <span>Feed</span>
        </button>
        <button 
          className={`nav-btn ${activeTab === 'map' ? 'active' : ''}`}
          onClick={() => { setActiveTab('map'); triggerHaptic(); }}
        >
          <Share2 size={24} />
          <span>Graph</span>
        </button>
        <button 
          className={`nav-btn ${activeTab === 'models' ? 'active' : ''}`}
          onClick={() => { setActiveTab('models'); triggerHaptic(); }}
        >
          <BrainCircuit size={24} />
          <span>Intelligence</span>
        </button>
      </nav>

      {/* Native Undo Toast */}
      <AnimatePresence>
        {undoHistory.length > 0 && (
          <motion.div 
            initial={{ y: 100, opacity: 0 }}
            animate={{ y: -80, opacity: 1 }}
            exit={{ y: 100, opacity: 0 }}
            className="native-toast"
          >
            <span>Change committed</span>
            <button 
              onClick={() => { undo(); triggerHaptic(); }}
              style={{ background: 'transparent', border: '1px solid var(--primary)', color: 'var(--primary)', padding: '6px 12px', borderRadius: '16px', display: 'flex', alignItems: 'center', gap: '4px' }}
            >
              <Undo2 size={16} /> Undo
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      <style>{`
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
      `}</style>
    </div>
  );
}

export default App;
