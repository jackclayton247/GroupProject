import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './TrackOrderPage.css';

const statusSteps = ['received', 'Processing', 'Dispatched', 'Delivered'];

const statusLabels = {
  received: 'Order Received',
  Processing: 'Processing',
  Dispatched: 'Dispatched',
  Delivered: 'Delivered',
};

function TrackOrderPage() {
  const { orderId: urlOrderId } = useParams();
  const navigate = useNavigate();
  const [inputId, setInputId] = useState(urlOrderId || '');
  const [orderStatus, setOrderStatus] = useState(null);
  const [trackedId, setTrackedId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchStatus = async (id) => {
    const cleanId = id.replace(/^PU-/i, '');
    if (!cleanId || isNaN(cleanId)) {
      setError('Please enter a valid order number.');
      return;
    }
    setLoading(true);
    setError(null);
    setOrderStatus(null);
    try {
      const res = await fetch(`http://localhost:8080/api/orders/track/${cleanId}`);
      const text = await res.text();
      if (text === 'order not found' || text === 'error') {
        setError('Order not found. Please check the order number and try again.');
      } else {
        setOrderStatus(text);
        setTrackedId(id.startsWith('PU-') ? id : `PU-${cleanId}`);
      }
    } catch {
      setError('Could not connect to the server. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  // Auto-fetch if URL has an orderId
  React.useEffect(() => {
    if (urlOrderId) {
      fetchStatus(urlOrderId);
    }
  }, [urlOrderId]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (inputId.trim()) {
      navigate(`/track-order/${inputId.trim()}`, { replace: true });
      fetchStatus(inputId.trim());
    }
  };

  const currentStep = statusSteps.indexOf(orderStatus);

  return (
    <div className="track-order-page">
      <div className="to-header">
        <div className="to-header-content">
          <h1>Track Your Order</h1>
          <p>Enter your order number to see the latest status.</p>
        </div>
      </div>

      <div className="to-container">
        <form className="to-search-form" onSubmit={handleSubmit}>
          <input
            type="text"
            className="to-search-input"
            placeholder="e.g. PU-42 or 42"
            value={inputId}
            onChange={(e) => setInputId(e.target.value)}
          />
          <button type="submit" className="to-search-btn" disabled={loading}>
            {loading ? 'Searching...' : 'Track'}
          </button>
        </form>

        {error && (
          <div className="to-error">
            <p>{error}</p>
          </div>
        )}

        {orderStatus && (
          <div className="to-result">
            <div className="to-order-id">Order {trackedId}</div>

            <div className="to-progress">
              {statusSteps.map((step, idx) => (
                <div key={step} className={`to-step ${idx <= currentStep ? 'completed' : ''} ${idx === currentStep ? 'current' : ''}`}>
                  <div className="to-step-dot">
                    {idx < currentStep ? (
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
                    ) : (
                      <span>{idx + 1}</span>
                    )}
                  </div>
                  {idx < statusSteps.length - 1 && <div className="to-step-line" />}
                  <div className="to-step-label">{statusLabels[step]}</div>
                </div>
              ))}
            </div>

            <div className="to-status-badge-container">
              <span className={`ph-order-status status-${orderStatus === 'received' ? 'pending' : orderStatus.toLowerCase()}`}>
                {orderStatus === 'received' ? 'Pending' : orderStatus}
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default TrackOrderPage;
