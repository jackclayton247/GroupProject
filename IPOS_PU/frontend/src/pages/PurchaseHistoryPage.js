import React, { useState, useEffect } from 'react';
import './PurchaseHistoryPage.css';
import { useAuth } from '../context/AuthContext';

const statusColors = {
  Delivered: 'status-delivered',
  Dispatched: 'status-dispatched',
  Processing: 'status-processing',
  Pending: 'status-pending',
  received: 'status-pending',
};

function PurchaseHistoryPage() {
  const { isLoggedIn, userEmail } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedOrder, setExpandedOrder] = useState(null);
  const [filterStatus, setFilterStatus] = useState('All');

  const statuses = ['All', 'Pending', 'Processing', 'Dispatched', 'Delivered'];

  useEffect(() => {
    if (!isLoggedIn) {
      setLoading(false);
      return;
    }

    const fetchOrders = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/orders/my-orders?email=${encodeURIComponent(userEmail)}`, {
          credentials: 'include'
        });
        const data = await response.json();
        if (data.error) {
          setError(data.message);
        } else {
          setOrders(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        setError('Failed to load orders');
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [isLoggedIn]);

  if (!isLoggedIn) {
    return (
      <div className="purchase-history-page">
        <div className="ph-header">
          <div className="ph-header-content">
            <h1>Purchase History</h1>
          </div>
        </div>
        <div className="ph-container">
          <div className="ph-empty">
            <p>Please <a href="/login">log in</a> to view your purchase history.</p>
          </div>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="purchase-history-page">
        <div className="ph-header">
          <div className="ph-header-content">
            <h1>Purchase History</h1>
          </div>
        </div>
        <div className="ph-container">
          <div className="ph-empty">
            <p>Loading your orders...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="purchase-history-page">
        <div className="ph-header">
          <div className="ph-header-content">
            <h1>Purchase History</h1>
          </div>
        </div>
        <div className="ph-container">
          <div className="ph-empty">
            <p>Error: {error}</p>
          </div>
        </div>
      </div>
    );
  }

  const filtered = filterStatus === 'All'
    ? orders
    : orders.filter(o => o.status === filterStatus || (filterStatus === 'Pending' && o.status === 'received'));

  return (
    <div className="purchase-history-page">
      <div className="ph-header">
        <div className="ph-header-content">
          <h1>Purchase History</h1>
          <p>View and track all your previous orders from IPOS-PU.</p>
        </div>
      </div>

      <div className="ph-container">
        {/* Filter Bar */}
        <div className="ph-filter-bar">
          <span className="ph-filter-label">Filter by status:</span>
          <div className="ph-filter-tabs">
            {statuses.map(s => (
              <button
                key={s}
                className={`ph-filter-tab ${filterStatus === s ? 'active' : ''}`}
                onClick={() => setFilterStatus(s)}
              >
                {s}
              </button>
            ))}
          </div>
        </div>

        {/* Orders Table */}
        {filtered.length === 0 ? (
          <div className="ph-empty">
            <p>No orders found for this filter.</p>
          </div>
        ) : (
          <div className="ph-orders">
            {filtered.map(order => (
              <div key={order.orderId} className="ph-order-card">
                <div
                  className="ph-order-summary"
                  onClick={() => setExpandedOrder(expandedOrder === order.orderId ? null : order.orderId)}
                >
                  <div className="ph-order-info">
                    <span className="ph-order-id">{order.orderId}</span>
                    <span className="ph-order-date">
                      {new Date(order.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
                    </span>
                  </div>
                  <div className="ph-order-meta">
                    <span className={`ph-order-status ${statusColors[order.status] || ''}`}>{order.status}</span>
                    <span className="ph-order-total">&pound;{order.total.toFixed(2)}</span>
                    <span className="ph-expand-icon">{expandedOrder === order.orderId ? '▲' : '▼'}</span>
                  </div>
                </div>

                {expandedOrder === order.orderId && (
                  <div className="ph-order-items">
                    <table className="ph-items-table">
                      <thead>
                        <tr>
                          <th>Item</th>
                          <th>Unit Price</th>
                          <th>Qty</th>
                          <th>Subtotal</th>
                        </tr>
                      </thead>
                      <tbody>
                        {order.items.map((item, idx) => (
                          <tr key={idx}>
                            <td>{item.description}</td>
                            <td>&pound;{item.unitPrice.toFixed(2)}</td>
                            <td>{item.qty}</td>
                            <td>&pound;{(item.unitPrice * item.qty).toFixed(2)}</td>
                          </tr>
                        ))}
                      </tbody>
                      <tfoot>
                        <tr>
                          <td colSpan={3} className="ph-total-label">Total</td>
                          <td className="ph-total-value">&pound;{order.total.toFixed(2)}</td>
                        </tr>
                      </tfoot>
                    </table>
                    <div className="ph-order-actions">
                      <button className="ph-track-btn" onClick={() => alert(`Order #${order.orderId}\nStatus: ${order.status}\nDate: ${new Date(order.date).toLocaleDateString('en-GB')}\nTotal: £${order.total.toFixed(2)}`)}>Track Order</button>
                      {order.status === 'Delivered' && (
                        <button className="ph-reorder-btn">Reorder</button>
                      )}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default PurchaseHistoryPage;
