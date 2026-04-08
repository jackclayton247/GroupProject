import React, { useState } from 'react';
import './PurchaseHistoryPage.css';

const mockOrders = [
  {
    orderId: 'PU-10045',
    date: '2026-04-05',
    status: 'Delivered',
    total: 18.75,
    items: [
      { description: 'Paracetamol', qty: 2, unitPrice: 0.10 },
      { description: 'Vitamin C', qty: 5, unitPrice: 1.20 },
      { description: 'Aspirin', qty: 10, unitPrice: 0.50 },
    ],
  },
  {
    orderId: 'PU-10038',
    date: '2026-03-28',
    status: 'Dispatched',
    total: 32.50,
    items: [
      { description: 'Rhynol', qty: 3, unitPrice: 2.50 },
      { description: 'Vitamin B12', qty: 10, unitPrice: 1.30 },
      { description: 'Iodine tincture', qty: 8, unitPrice: 0.30 },
    ],
  },
  {
    orderId: 'PU-10021',
    date: '2026-03-10',
    status: 'Delivered',
    total: 55.00,
    items: [
      { description: 'Celebrex, caps 100 mg', qty: 3, unitPrice: 10.00 },
      { description: 'Claritin CR, 60g', qty: 1, unitPrice: 19.50 },
      { description: 'Analgin', qty: 2, unitPrice: 1.20 },
      { description: 'Aspirin', qty: 3, unitPrice: 0.50 },
    ],
  },
  {
    orderId: 'PU-10009',
    date: '2026-02-18',
    status: 'Delivered',
    total: 12.30,
    items: [
      { description: 'Paracetamol', qty: 5, unitPrice: 0.10 },
      { description: 'Vitamin C', qty: 8, unitPrice: 1.20 },
    ],
  },
];

const statusColors = {
  Delivered: 'status-delivered',
  Dispatched: 'status-dispatched',
  Processing: 'status-processing',
  Pending: 'status-pending',
};

function PurchaseHistoryPage() {
  const [expandedOrder, setExpandedOrder] = useState(null);
  const [filterStatus, setFilterStatus] = useState('All');

  const statuses = ['All', 'Pending', 'Processing', 'Dispatched', 'Delivered'];

  const filtered = filterStatus === 'All'
    ? mockOrders
    : mockOrders.filter(o => o.status === filterStatus);

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
                      <button className="ph-track-btn">Track Order</button>
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
