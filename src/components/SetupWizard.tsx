import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { BrainCircuit, Database, Shield, ChevronRight } from 'lucide-react';
import { useContinuumStore } from '../store';

export const SetupWizard: React.FC = () => {
  const [step, setStep] = useState(0);
  const completeSetup = useContinuumStore((state) => state.completeSetup);

  const steps = [
    {
      title: "Welcome to Continuum",
      description: "Your local-first, AI-powered project and context manager.",
      icon: <Database size={48} color="var(--primary)" />,
      content: "Continuum acts as your persistent memory. It ties notes, tasks, and ideas directly to your projects, keeping you focused and eliminating context switching."
    },
    {
      title: "Your Private AI",
      description: "Local intelligence that respects your privacy.",
      icon: <BrainCircuit size={48} color="var(--primary)" />,
      content: "Instead of sending your thoughts to the cloud, Continuum connects to local models (like GPT4All or Ollama). The AI acts as your partner, drafting tasks and organizing notes offline."
    },
    {
      title: "Total Control",
      description: "AI is a writer, not just an advisor.",
      icon: <Shield size={48} color="var(--primary)" />,
      content: "When the AI suggests changes or extracts tasks from your notes, it proposes them as 'Mutations'. You are always in control—review, approve, or undo them at any time."
    }
  ];

  const nextStep = () => {
    if (step < steps.length - 1) {
      setStep(step + 1);
    } else {
      completeSetup();
    }
  };

  return (
    <div className="wizard-overlay">
      <motion.div 
        className="wizard-card"
        initial={{ opacity: 0, scale: 0.9, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ type: "spring", damping: 25, stiffness: 300 }}
      >
        <div className="wizard-progress">
          {steps.map((_, i) => (
            <div key={i} className={`progress-dot ${i === step ? 'active' : ''}`} />
          ))}
        </div>

        <AnimatePresence mode="wait">
          <motion.div 
            key={step}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.2 }}
            className="wizard-content"
          >
            <div className="wizard-icon-wrapper">
              {steps[step]?.icon}
            </div>
            <h2>{steps[step]?.title}</h2>
            <h4>{steps[step]?.description}</h4>
            <p>{steps[step]?.content}</p>
          </motion.div>
        </AnimatePresence>

        <button className="wizard-btn" onClick={nextStep}>
          {step === steps.length - 1 ? 'Get Started' : 'Next'}
          <ChevronRight size={20} />
        </button>
      </motion.div>

      <style>{`
        .wizard-overlay {
          position: fixed;
          inset: 0;
          background: rgba(0, 0, 0, 0.85);
          backdrop-filter: blur(8px);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 9999;
          padding: 24px;
        }

        .wizard-card {
          background: var(--surface);
          border-radius: var(--radius-lg);
          padding: 32px;
          max-width: 400px;
          width: 100%;
          text-align: center;
          display: flex;
          flex-direction: column;
          gap: 24px;
          border: 1px solid var(--surface-variant);
          box-shadow: 0 8px 32px rgba(0,0,0,0.5);
        }

        .wizard-progress {
          display: flex;
          justify-content: center;
          gap: 8px;
          margin-bottom: 8px;
        }

        .progress-dot {
          width: 8px;
          height: 8px;
          border-radius: 4px;
          background: var(--surface-variant);
          transition: all 0.3s ease;
        }

        .progress-dot.active {
          background: var(--primary);
          width: 24px;
        }

        .wizard-icon-wrapper {
          display: flex;
          justify-content: center;
          margin-bottom: 24px;
          padding: 24px;
          background: var(--secondary-container);
          border-radius: 50%;
          width: 96px;
          height: 96px;
          margin: 0 auto 24px;
        }

        .wizard-content h2 {
          margin: 0 0 8px 0;
          font-size: 1.5rem;
          color: var(--on-surface);
        }

        .wizard-content h4 {
          margin: 0 0 16px 0;
          font-size: 1rem;
          color: var(--primary);
          font-weight: 500;
        }

        .wizard-content p {
          color: #A09E9F;
          line-height: 1.5;
          margin: 0;
          font-size: 0.95rem;
        }

        .wizard-btn {
          background: var(--primary-container);
          color: var(--primary);
          border: none;
          padding: 16px;
          border-radius: var(--radius-md);
          font-size: 1.1rem;
          font-weight: 600;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          cursor: pointer;
          margin-top: 8px;
          transition: background 0.2s ease;
        }

        .wizard-btn:hover {
          background: #5d42a6; /* slightly lighter primary container */
        }
      `}</style>
    </div>
  );
};
