import React, { useState } from 'react';
import './MerchantDashboard.css';

// ── Mock data ──────────────────────────────────────────────────────────────────
const mockCampaigns = [
  {
    id: 'CAMP_001',
    name: 'Spring Sale',
    startDate: '2026-04-01',
    endDate: '2026-04-30',
    status: 'Active',
    clicks: 142,
    items: [
      { itemId: '100 00001', description: 'Paracetamol', discount: 20, added: 38, purchased: 25 },
      { itemId: '400 00001', description: 'Vitamin C', discount: 10, added: 55, purchased: 41 },
    ],
  },
  {
    id: 'CAMP_002',
    name: 'Wellness Week',
    startDate: '2026-04-08',
    endDate: '2026-04-15',
    status: 'Active',
    clicks: 67,
    items: [
      { itemId: '400 00002', description: 'Vitamin B12', discount: 25, added: 22, purchased: 14 },
      { itemId: '200 00005', description: 'Rhynol', discount: 10, added: 11, purchased: 8 },
    ],
  },
  {
    id: 'CAMP_000',
    name: 'Winter Clearance',
    startDate: '2026-01-10',
    endDate: '2026-01-31',
    status: 'Ended',
    clicks: 309,
    items: [
      { itemId: '100 00003', description: 'Analgin', discount: 30, added: 110, purchased: 98 },
    ],
  },
];

const mockSalesReport = [
  { itemId: '100 00001', description: 'Paracetamol', unitPrice: 0.10, qtySold: 250, revenue: 25.00 },
  { itemId: '100 00002', description: 'Aspirin', unitPrice: 0.50, qtySold: 180, revenue: 90.00 },
  { itemId: '400 00001', description: 'Vitamin C', unitPrice: 1.20, qtySold: 95, revenue: 114.00 },
  { itemId: '400 00002', description: 'Vitamin B12', unitPrice: 1.30, qtySold: 80, revenue: 104.00 },
  { itemId: '200 00005', description: 'Rhynol', unitPrice: 2.50, qtySold: 42, revenue: 105.00 },
];

const availableProducts = [
  { itemId: '100 00001', description: 'Paracetamol', unitCost: 0.10 },
  { itemId: '100 00002', description: 'Aspirin', unitCost: 0.50 },
  { itemId: '100 00003', description: 'Analgin', unitCost: 1.20 },
  { itemId: '400 00001', description: 'Vitamin C', unitCost: 1.20 },
  { itemId: '400 00002', description: 'Vitamin B12', unitCost: 1.30 },
  { itemId: '200 00005', description: 'Rhynol', unitCost: 2.50 },
];

// ── Sub-components ─────────────────────────────────────────────────────────────

function PromotionsTab() {
  const [campaigns, setCampaigns] = useState(mockCampaigns);
  const [showForm, setShowForm] = useState(false);
  const [expandedCamp, setExpandedCamp] = useState(null);
  const [newCamp, setNewCamp] = useState({
    name: '',
    startDate: '',
    endDate: '',
    selectedItems: {},
  });

  const handleCancel = (id) => {
    setCampaigns(prev =>
      prev.map(c => c.id === id ? { ...c, status: 'Cancelled' } : c)
    );
  };

  const handleItemToggle = (itemId) => {
    setNewCamp(prev => {
      const updated = { ...prev.selectedItems };
      if (updated[itemId]) {
        delete updated[itemId];
      } else {
        updated[itemId] = 10;
      }
      return { ...prev, selectedItems: updated };
    });
  };

  const handleDiscountChange = (itemId, val) => {
    setNewCamp(prev => ({
      ...prev,
      selectedItems: { ...prev.selectedItems, [itemId]: Number(val) },
    }));
  };

  const handleCreateCampaign = (e) => {
    e.preventDefault();
    const items = Object.entries(newCamp.selectedItems).map(([itemId, discount]) => {
      const prod = availableProducts.find(p => p.itemId === itemId);
      return { itemId, description: prod.description, discount, added: 0, purchased: 0 };
    });
    const camp = {
      id: `CAMP_${Date.now()}`,
      name: newCamp.name,
      startDate: newCamp.startDate,
      endDate: newCamp.endDate,
      status: 'Active',
      clicks: 0,
      items,
    };
    setCampaigns(prev => [camp, ...prev]);
    setNewCamp({ name: '', startDate: '', endDate: '', selectedItems: {} });
    setShowForm(false);
  };

  const statusClass = { Active: 'badge-active', Ended: 'badge-ended', Cancelled: 'badge-cancelled' };

  return (
    <div className="tab-content">
      <div className="tab-toolbar">
        <h2>Promotion Campaigns</h2>
        <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ New Campaign'}
        </button>
      </div>

      {/* Create Campaign Form */}
      {showForm && (
        <form className="create-form" onSubmit={handleCreateCampaign}>
          <h3>Create New Campaign</h3>
          <div className="form-grid">
            <div className="form-group">
              <label>Campaign Name</label>
              <input
                type="text"
                placeholder="e.g. Summer Deal"
                value={newCamp.name}
                onChange={e => setNewCamp(prev => ({ ...prev, name: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Start Date</label>
              <input
                type="date"
                value={newCamp.startDate}
                onChange={e => setNewCamp(prev => ({ ...prev, startDate: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input
                type="date"
                value={newCamp.endDate}
                onChange={e => setNewCamp(prev => ({ ...prev, endDate: e.target.value }))}
                required
              />
            </div>
          </div>

          <div className="form-section">
            <label>Select Products &amp; Discounts</label>
            <div className="product-select-grid">
              {availableProducts.map(prod => {
                const selected = prod.itemId in newCamp.selectedItems;
                return (
                  <div
                    key={prod.itemId}
                    className={`product-select-card ${selected ? 'selected' : ''}`}
                    onClick={() => handleItemToggle(prod.itemId)}
                  >
                    <span className="ps-name">{prod.description}</span>
                    <span className="ps-price">&pound;{prod.unitCost.toFixed(2)}</span>
                    {selected && (
                      <div className="ps-discount-row" onClick={e => e.stopPropagation()}>
                        <label>Discount %</label>
                        <input
                          type="number"
                          min="1"
                          max="99"
                          value={newCamp.selectedItems[prod.itemId]}
                          onChange={e => handleDiscountChange(prod.itemId, e.target.value)}
                        />
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          <button
            type="submit"
            className="btn-primary"
            disabled={!newCamp.name || !newCamp.startDate || !newCamp.endDate || Object.keys(newCamp.selectedItems).length === 0}
          >
            Create Campaign
          </button>
        </form>
      )}

      {/* Campaigns Table */}
      <div className="campaigns-table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Period</th>
              <th>Status</th>
              <th>Clicks</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {campaigns.map(camp => (
              <React.Fragment key={camp.id}>
                <tr
                  className="clickable-row"
                  onClick={() => setExpandedCamp(expandedCamp === camp.id ? null : camp.id)}
                >
                  <td>{camp.id}</td>
                  <td>{camp.name}</td>
                  <td>
                    {new Date(camp.startDate).toLocaleDateString('en-GB')} &ndash;{' '}
                    {new Date(camp.endDate).toLocaleDateString('en-GB')}
                  </td>
                  <td><span className={`badge ${statusClass[camp.status]}`}>{camp.status}</span></td>
                  <td>{camp.clicks}</td>
                  <td>
                    {camp.status === 'Active' && (
                      <button
                        className="btn-danger-sm"
                        onClick={e => { e.stopPropagation(); handleCancel(camp.id); }}
                      >
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
                {expandedCamp === camp.id && (
                  <tr className="expanded-row">
                    <td colSpan={6}>
                      <table className="inner-table">
                        <thead>
                          <tr>
                            <th>Item</th>
                            <th>Discount</th>
                            <th>Added to Cart</th>
                            <th>Purchased</th>
                            <th>Conversion Rate</th>
                          </tr>
                        </thead>
                        <tbody>
                          {camp.items.map(item => (
                            <tr key={item.itemId}>
                              <td>{item.description}</td>
                              <td>{item.discount}%</td>
                              <td>{item.added}</td>
                              <td>{item.purchased}</td>
                              <td>
                                {item.added > 0
                                  ? ((item.purchased / item.added) * 100).toFixed(1) + '%'
                                  : '—'}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </td>
                  </tr>
                )}
              </React.Fragment>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function ReportsTab() {
  const [period, setPeriod] = useState({ from: '2026-01-01', to: '2026-04-08' });

  const totalRevenue = mockSalesReport.reduce((s, r) => s + r.revenue, 0);
  const totalQty = mockSalesReport.reduce((s, r) => s + r.qtySold, 0);

  return (
    <div className="tab-content">
      <div className="tab-toolbar">
        <h2>Sales Report</h2>
        <div className="period-filter">
          <label>From <input type="date" value={period.from} onChange={e => setPeriod(p => ({ ...p, from: e.target.value }))} /></label>
          <label>To <input type="date" value={period.to} onChange={e => setPeriod(p => ({ ...p, to: e.target.value }))} /></label>
          <button className="btn-secondary">Generate</button>
        </div>
      </div>

      <div className="report-summary-cards">
        <div className="summary-card">
          <span className="summary-label">Total Revenue</span>
          <span className="summary-value">&pound;{totalRevenue.toFixed(2)}</span>
        </div>
        <div className="summary-card">
          <span className="summary-label">Units Sold</span>
          <span className="summary-value">{totalQty}</span>
        </div>
        <div className="summary-card">
          <span className="summary-label">Products</span>
          <span className="summary-value">{mockSalesReport.length}</span>
        </div>
      </div>

      <div className="campaigns-table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Item ID</th>
              <th>Description</th>
              <th>Unit Price</th>
              <th>Qty Sold</th>
              <th>Revenue</th>
            </tr>
          </thead>
          <tbody>
            {mockSalesReport.map(row => (
              <tr key={row.itemId}>
                <td>{row.itemId}</td>
                <td>{row.description}</td>
                <td>&pound;{row.unitPrice.toFixed(2)}</td>
                <td>{row.qtySold}</td>
                <td>&pound;{row.revenue.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={3} className="tfoot-label">Totals</td>
              <td><strong>{totalQty}</strong></td>
              <td><strong>&pound;{totalRevenue.toFixed(2)}</strong></td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
}

// ── Main Dashboard ─────────────────────────────────────────────────────────────
function MerchantDashboard() {
  const [activeTab, setActiveTab] = useState('promotions');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [loginError, setLoginError] = useState('');

  const handleLogin = (e) => {
    e.preventDefault();
    // Frontend-only placeholder: accept any non-empty credentials
    if (credentials.email && credentials.password) {
      setIsLoggedIn(true);
      setLoginError('');
    } else {
      setLoginError('Please enter your email and password.');
    }
  };

  if (!isLoggedIn) {
    return (
      <div className="merchant-login-page">
        <div className="merchant-login-card">
          <div className="ml-logo">IPOS-PU</div>
          <h2>Merchant Portal</h2>
          <p className="ml-subtitle">Sign in to manage your promotions and reports.</p>

          {loginError && <p className="ml-error">{loginError}</p>}

          <form onSubmit={handleLogin} className="ml-form">
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                placeholder="merchant@pharmacy.com"
                value={credentials.email}
                onChange={e => setCredentials(p => ({ ...p, email: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                placeholder="Enter password"
                value={credentials.password}
                onChange={e => setCredentials(p => ({ ...p, password: e.target.value }))}
                required
              />
            </div>
            <button type="submit" className="ml-submit-btn">Sign In</button>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="merchant-dashboard">
      {/* Sidebar */}
      <aside className="md-sidebar">
        <div className="md-brand">
          <span className="md-brand-logo">IPOS-PU</span>
          <span className="md-brand-sub">Merchant Portal</span>
        </div>
        <nav className="md-nav">
          <button
            className={`md-nav-item ${activeTab === 'promotions' ? 'active' : ''}`}
            onClick={() => setActiveTab('promotions')}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>
            Promotions
          </button>
          <button
            className={`md-nav-item ${activeTab === 'reports' ? 'active' : ''}`}
            onClick={() => setActiveTab('reports')}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
            Reports
          </button>
        </nav>
        <button className="md-logout" onClick={() => setIsLoggedIn(false)}>
          Sign Out
        </button>
      </aside>

      {/* Main Content */}
      <main className="md-main">
        {activeTab === 'promotions' && <PromotionsTab />}
        {activeTab === 'reports' && <ReportsTab />}
      </main>
    </div>
  );
}

export default MerchantDashboard;
