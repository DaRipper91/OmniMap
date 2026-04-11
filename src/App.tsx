import React from 'react';
import { useContinuumStore } from './store';
import { getChildren } from './graph-utils';
import { nativeFS, getPlatform } from './native-bridge-impl';
import { MindMap } from './components/MindMap';
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
  ArrowRightLeft
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Haptics, ImpactStyle, NotificationType } from '@capacitor/haptics';

function App() {
  const nodes = useContinuumStore((state) => state.nodes);
  const edges = useContinuumStore((state) => state.edges);
  const models = useContinuumStore((state) => state.models);
  const runtimes = useContinuumStore((state) => state.runtimes);
  const downloadProgress = useContinuumStore((state) => state.downloadProgress);
  const requestAI = useContinuumStore((state) => state.requestAI);
  const downloadModel = useContinuumStore((state) => state.downloadModel);
  const suggestedActions = useContinuumStore((state) => state.suggestedActions);
  const undoHistory = useContinuumStore((state) => state.undoHistory);
  const activeProjectId = useContinuumStore((state) => state.activeProjectId);
  const setActiveProject = useContinuumStore((state) => state.setActiveProject);
  const proposeMutation = useContinuumStore((state) => state.proposeMutation);
  const commitMutation = useContinuumStore((state) => state.commitMutation);
  const addNote = useContinuumStore((state) => state.addNote);
  const acceptAction = useContinuumStore((state) => state.acceptAction);
  const snoozeAction = useContinuumStore((state) => state.snoozeAction);
  const archiveAction = useContinuumStore((state) => state.archiveAction);
  const undo = useContinuumStore((state) => state.undo);
  const pendingMutations = useContinuumStore((state) => state.pendingMutations);

  const [activeTab, setActiveTab] = React.useState<'dashboard' | 'map' | 'models'>('dashboard');
  const [isProjectDrawerOpen, setIsProjectDrawerOpen] = React.useState(false);
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

  const handleQuickCapture = (e: React.FormEvent<HTMLFormElement>) => {
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
  };

  return (
    <div className="app-container native-shell">
      {/* Immersive Top Header */}
      <header className="native-app-bar">
        <button className="icon-btn" onClick={() => { setIsProjectDrawerOpen(true); triggerHaptic(); }}>
          <Menu size={24} />
        </button>
        <div className="app-bar-title">
          {activeProject ? activeProject.title : 'Continuum'}
        </div>
        <div className="app-bar-actions">
          <button className="icon-btn" onClick={() => triggerHaptic()}><Search size={22} /></button>
          <button className="icon-btn" onClick={() => triggerHaptic()}><Settings size={22} /></button>
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
                  <div className="card-header">
                    <h3>Approvals Required</h3>
                    <span className="badge">{pendingMutations.length}</span>
                  </div>
                  <div className="mutation-stack">
                    {pendingMutations.map(m => (
                      <div key={m.id} className="mutation-row">
                        <span>{m.type} {m.entity}</span>
                        <button className="chip-btn" onClick={() => { commitMutation(m.id); triggerHaptic(); }}>Approve</button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Quick Capture (MD3 Input) */}
              <div className="capture-surface">
                <form onSubmit={handleQuickCapture}>
                  <textarea name="note-content" placeholder="Capture a thought..." rows={1} />
                  <button type="submit" className="fab-mini"><Plus size={20} /></button>
                </form>
              </div>

              {/* Task Section */}
              <div className="native-section">
                <div className="section-title">
                  Tasks <span>{filteredTasks.length}</span>
                </div>
                <div className="native-task-list">
                  {filteredTasks.map(task => (
                    <motion.div 
                      key={task.id} 
                      whileTap={{ scale: 0.98 }}
                      className={`native-task-card status-${task.data?.status || 'todo'}`}
                      onClick={() => handleToggleStatus(task)}
                    >
                      <div className="task-indicator"></div>
                      <div className="task-content">
                        <h4>{task.title}</h4>
                        <p>{task.description}</p>
                      </div>
                      <ArrowRightLeft size={16} className="task-icon" />
                    </motion.div>
                  ))}
                </div>
              </div>

              {/* Notes Feed */}
              <div className="native-section">
                <div className="section-title">Recent Notes</div>
                <div className="native-note-stack">
                  {filteredNotes.map(note => (
                    <div key={note.id} className="native-note-card">
                      {note.data?.content || note.description}
                    </div>
                  ))}
                </div>
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
              <div className="section-title">AI Runtimes</div>
              {/* Model Cards... */}
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
              <h3>Select Project</h3>
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
            <button onClick={() => { undo(); triggerHaptic(); }}><Undo2 size={18} /> Undo</button>
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
        }

        .nav-btn.active { opacity: 1; color: var(--primary); }
        .nav-btn.active span { font-weight: 700; }
        .nav-btn span { font-size: 0.75rem; }

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

        .icon-btn { background: transparent; border: none; color: var(--on-surface); padding: 8px; border-radius: 50%; }
        .native-shell * { transition: background-color 0.2s; }
      `}</style>
    </div>
  );
}

export default App;
