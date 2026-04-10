import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getMessages, sendMessage, markMessagesRead } from '../api';
import ServiceUnavailable from '../components/ServiceUnavailable';

export default function ConversationPage() {
  const { reservationId } = useParams();
  const navigate = useNavigate();
  const userId = localStorage.getItem('userId');
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [serviceDown, setServiceDown] = useState(false);
  const [sending, setSending] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    loadMessages();
  }, [reservationId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  async function loadMessages() {
    setLoading(true);
    setServiceDown(false);
    try {
      const data = await getMessages(reservationId);
      setMessages(data || []);
      try { await markMessagesRead(reservationId, 'OWNER'); } catch {}
    } catch (err) {
      if (err.status === 503) setServiceDown(true);
    }
    setLoading(false);
  }

  async function handleSend(e) {
    e.preventDefault();
    if (!newMessage.trim() || sending) return;
    setSending(true);
    try {
      await sendMessage({
        reservationId: parseInt(reservationId),
        senderId: parseInt(userId),
        senderRole: 'OWNER',
        content: newMessage.trim(),
      });
      setNewMessage('');
      loadMessages();
    } catch (err) {
      if (err.status === 503) setServiceDown(true);
    }
    setSending(false);
  }

  function formatTime(dateStr) {
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' }) + ' ' + d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  if (serviceDown) return <ServiceUnavailable message="Le service de messagerie est temporairement indisponible." onRetry={loadMessages} />;

  return (
    <div className="conversation">
      <div className="conversation-header">
        <button className="btn-back" onClick={() => navigate('/')}>
          <i className="fas fa-arrow-left"></i> Retour
        </button>
        <h2>Conversation — Réservation #{reservationId}</h2>
      </div>

      <div className="messages-list">
        {loading && <p style={{textAlign:'center', color:'#94a3b8'}}>Chargement...</p>}
        {!loading && messages.length === 0 && (
          <p style={{textAlign:'center', color:'#94a3b8', marginTop:'2rem'}}>
            Aucun message. Commencez la conversation !
          </p>
        )}
        {messages.map((m) => (
          <div key={m.id} className={`message-bubble ${m.senderRole === 'OWNER' ? 'message-mine' : 'message-other'}`}>
            <div className="message-content">{m.content}</div>
            <div className="message-time">
              {formatTime(m.sentAt)}
              {m.senderRole === 'OWNER' && (
                <span className={`message-status ${m.read ? 'message-status-read' : 'message-status-sent'}`}>
                  {m.read ? <><i className="fas fa-check-double"></i> Lu</> : <><i className="fas fa-check"></i> Envoyé</>}
                </span>
              )}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <form className="message-input" onSubmit={handleSend}>
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="Écrire un message..."
          disabled={sending}
        />
        <button type="submit" disabled={sending || !newMessage.trim()}>
          <i className="fas fa-paper-plane"></i>
        </button>
      </form>
    </div>
  );
}
