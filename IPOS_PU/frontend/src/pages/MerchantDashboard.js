import React, { useState, useEffect } from 'react';
import './MerchantDashboard.css';
import { useAuth } from '../context/AuthContext';

const API = 'http://localhost:8080';

// ── Sub-components ─────────────────────────────────────────────────────────────

function PromotionsTab() {
  const [campaigns, setCampaigns] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [expandedCamp, setExpandedCamp] = useState(null);
  const [availableProducts, setAvailableProducts] = useState([]);
  const [editingCamp, setEditingCamp] = useState(null); // campaign name being edited
  const [editData, setEditData] = useState({
    startDate: '',
    endDate: '',
    selectedItems: {},   // { productId: discount }
    originalItems: {},   // to track what was there before
  });
  const [newCamp, setNewCamp] = useState({
    name: '',
    startDate: '',
    endDate: '',
    selectedItems: {},
  });

  useEffect(() => {
    fetchCampaigns();
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const res = await fetch(`${API}/api/products`);
      const data = await res.json();
      setAvailableProducts(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const fetchCampaigns = async () => {
    try {
      const res = await fetch(`${API}/promo/active`);
      const data = await res.json();
      if (Array.isArray(data)) {
        // Fetch engagement data for each campaign
        const campaignsWithStats = await Promise.all(data.map(async (camp) => {
          try {
            const engRes = await fetch(`${API}/api/reports/engagement?campaignName=${encodeURIComponent(camp.name)}`);
            const eng = await engRes.json();
            return {
              ...camp,
              id: camp.name,
              status: new Date(camp.endDate) >= new Date() ? 'Active' : 'Ended',
              clicks: eng.campaignHits || 0,
              items: camp.items.map(item => {
                const engItem = (eng.items || []).find(e => e.productId === item.productId);
                return {
                  ...item,
                  added: engItem ? engItem.hitsCount : 0,
                  purchased: engItem ? engItem.purchases : 0,
                };
              }),
            };
          } catch {
            return {
              ...camp,
              id: camp.name,
              status: new Date(camp.endDate) >= new Date() ? 'Active' : 'Ended',
              clicks: 0,
              items: camp.items.map(item => ({ ...item, added: 0, purchased: 0 })),
            };
          }
        }));
        setCampaigns(campaignsWithStats);
      }
    } catch (err) {
      console.error('Failed to fetch campaigns:', err);
    }
  };

  const handleCancel = async (name) => {
    try {
      await fetch(`${API}/promo/cancel?name=${encodeURIComponent(name)}`, { method: 'POST' });
      setCampaigns(prev => prev.filter(c => c.name !== name));
    } catch (err) {
      console.error('Failed to cancel campaign:', err);
    }
  };

  const handleStartEdit = (camp) => {
    const items = {};
    camp.items.forEach(item => {
      items[item.productId] = item.discount;
    });
    setEditingCamp(camp.name);
    setEditData({
      startDate: camp.startDate,
      endDate: camp.endDate,
      selectedItems: { ...items },
      originalItems: { ...items },
    });
    setShowForm(false);
  };

  const handleEditItemToggle = (productId) => {
    setEditData(prev => {
      const updated = { ...prev.selectedItems };
      if (updated[productId] !== undefined) {
        delete updated[productId];
      } else {
        updated[productId] = 10;
      }
      return { ...prev, selectedItems: updated };
    });
  };

  const handleEditDiscountChange = (productId, val) => {
    setEditData(prev => ({
      ...prev,
      selectedItems: { ...prev.selectedItems, [productId]: Number(val) },
    }));
  };

  const handleSaveEdit = async (e) => {
    e.preventDefault();
    try {
      // Update dates
      const dateRes = await fetch(`${API}/promo/update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: editingCamp,
          start: editData.startDate,
          end: editData.endDate,
        }),
      });
      const dateResult = await dateRes.text();
      if (!dateResult.includes('Success')) {
        alert('Failed to update campaign dates: ' + dateResult);
        return;
      }

      const oldIds = Object.keys(editData.originalItems).map(Number);
      const newIds = Object.keys(editData.selectedItems).map(Number);

      // Remove products that were deselected
      for (const id of oldIds) {
        if (!newIds.includes(id)) {
          await fetch(`${API}/promo-product/remove?productId=${id}&promotionName=${encodeURIComponent(editingCamp)}`, { method: 'POST' });
        }
      }

      // Add new products
      for (const id of newIds) {
        if (!oldIds.includes(id)) {
          await fetch(`${API}/promo-product/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              productId: id,
              discount: editData.selectedItems[id],
              promotionName: editingCamp,
            }),
          });
        }
      }

      // Update discounts for existing products that changed
      for (const id of newIds) {
        if (oldIds.includes(id) && editData.selectedItems[id] !== editData.originalItems[id]) {
          await fetch(`${API}/promo-product/update`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              productId: id,
              discount: editData.selectedItems[id],
              promotionName: editingCamp,
            }),
          });
        }
      }

      setEditingCamp(null);
      fetchCampaigns();
    } catch (err) {
      console.error('Failed to update campaign:', err);
      alert('Error updating campaign');
    }
  };

  const handleItemToggle = (productId) => {
    setNewCamp(prev => {
      const updated = { ...prev.selectedItems };
      if (updated[productId]) {
        delete updated[productId];
      } else {
        updated[productId] = 10;
      }
      return { ...prev, selectedItems: updated };
    });
  };

  const handleDiscountChange = (productId, val) => {
    setNewCamp(prev => ({
      ...prev,
      selectedItems: { ...prev.selectedItems, [productId]: Number(val) },
    }));
  };

  const handleCreateCampaign = async (e) => {
    e.preventDefault();

    try {
      // Create the promotion
      const createRes = await fetch(`${API}/promo/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: newCamp.name,
          start: newCamp.startDate,
          end: newCamp.endDate,
        }),
      });
      const createResult = await createRes.text();
      if (!createRes.ok || !createResult.includes('Success')) {
        alert('Failed to create campaign: ' + createResult);
        return;
      }

      // Add products to the promotion
      for (const [productId, discount] of Object.entries(newCamp.selectedItems)) {
        await fetch(`${API}/promo-product/add`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            productId: parseInt(productId),
            discount: discount,
            promotionName: newCamp.name,
          }),
        });
      }

      setNewCamp({ name: '', startDate: '', endDate: '', selectedItems: {} });
      setShowForm(false);
      fetchCampaigns();
    } catch (err) {
      console.error('Failed to create campaign:', err);
      alert('Error creating campaign');
    }
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
                const selected = prod.productId in newCamp.selectedItems;
                return (
                  <div
                    key={prod.productId}
                    className={`product-select-card ${selected ? 'selected' : ''}`}
                    onClick={() => handleItemToggle(prod.productId)}
                  >
                    <span className="ps-name">{prod.description}</span>
                    <span className="ps-price">&pound;{(prod.price || 0).toFixed(2)}</span>
                    {selected && (
                      <div className="ps-discount-row" onClick={e => e.stopPropagation()}>
                        <label>Discount %</label>
                        <input
                          type="number"
                          min="1"
                          max="99"
                          value={newCamp.selectedItems[prod.productId]}
                          onChange={e => handleDiscountChange(prod.productId, e.target.value)}
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

      {editingCamp && (
        <form className="create-form edit-form" onSubmit={handleSaveEdit}>
          <div className="edit-form-header">
            <h3>Edit Campaign: {editingCamp}</h3>
            <button type="button" className="btn-secondary" onClick={() => setEditingCamp(null)}>Cancel</button>
          </div>
          <div className="form-grid">
            <div className="form-group">
              <label>Start Date</label>
              <input
                type="date"
                value={editData.startDate}
                onChange={e => setEditData(prev => ({ ...prev, startDate: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input
                type="date"
                value={editData.endDate}
                onChange={e => setEditData(prev => ({ ...prev, endDate: e.target.value }))}
                required
              />
            </div>
          </div>

          <div className="form-section">
            <label>Products &amp; Discounts</label>
            <div className="product-select-grid">
              {availableProducts.map(prod => {
                const selected = prod.productId in editData.selectedItems;
                return (
                  <div
                    key={prod.productId}
                    className={`product-select-card ${selected ? 'selected' : ''}`}
                    onClick={() => handleEditItemToggle(prod.productId)}
                  >
                    <span className="ps-name">{prod.description}</span>
                    <span className="ps-price">&pound;{(prod.price || 0).toFixed(2)}</span>
                    {selected && (
                      <div className="ps-discount-row" onClick={e => e.stopPropagation()}>
                        <label>Discount %</label>
                        <input
                          type="number"
                          min="1"
                          max="99"
                          value={editData.selectedItems[prod.productId]}
                          onChange={e => handleEditDiscountChange(prod.productId, e.target.value)}
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
            disabled={!editData.startDate || !editData.endDate || Object.keys(editData.selectedItems).length === 0}
          >
            Save Changes
          </button>
        </form>
      )}

      <div className="campaigns-table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Period</th>
              <th>Status</th>
              <th>Clicks</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {campaigns.length === 0 ? (
              <tr><td colSpan={5} style={{textAlign:'center', padding:'2rem'}}>No campaigns found. Create one to get started.</td></tr>
            ) : campaigns.map(camp => (
              <React.Fragment key={camp.name}>
                <tr
                  className="clickable-row"
                  onClick={() => setExpandedCamp(expandedCamp === camp.name ? null : camp.name)}
                >
                  <td>{camp.name}</td>
                  <td>
                    {new Date(camp.startDate).toLocaleDateString('en-GB')} &ndash;{' '}
                    {new Date(camp.endDate).toLocaleDateString('en-GB')}
                  </td>
                  <td><span className={`badge ${statusClass[camp.status]}`}>{camp.status}</span></td>
                  <td>{camp.clicks}</td>
                  <td>
                    {camp.status === 'Active' && (
                      <>
                        <button
                          className="btn-edit-sm"
                          onClick={e => { e.stopPropagation(); handleStartEdit(camp); }}
                        >
                          Edit
                        </button>
                        <button
                          className="btn-danger-sm"
                          onClick={e => { e.stopPropagation(); handleCancel(camp.name); }}
                        >
                          Cancel
                        </button>
                      </>
                    )}
                  </td>
                </tr>
                {expandedCamp === camp.name && (
                  <tr className="expanded-row">
                    <td colSpan={5}>
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
                            <tr key={item.productId}>
                              <td>{item.description}</td>
                              <td>{item.discount}%</td>
                              <td>{item.added}</td>
                              <td>{item.purchased}</td>
                              <td>
                                {item.added > 0
                                  ? ((item.purchased / item.added) * 100).toFixed(1) + '%'
                                  : '\u2014'}
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
  const [period, setPeriod] = useState({ from: '2026-01-01', to: new Date().toISOString().split('T')[0] });
  const [salesData, setSalesData] = useState(null);
  const [loading, setLoading] = useState(false);

  const downloadPDF = async () => {
    const { default: jsPDF } = await import('jspdf');
    const { default: autoTable } = await import('jspdf-autotable');

    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();

    // Title
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text('IPOS-PU Sales Report', pageWidth / 2, 20, { align: 'center' });

    // Period
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text(
      `Period: ${new Date(period.from).toLocaleDateString('en-GB')} \u2013 ${new Date(period.to).toLocaleDateString('en-GB')}`,
      pageWidth / 2, 28, { align: 'center' }
    );
    doc.text(`Generated: ${new Date().toLocaleDateString('en-GB')}`, pageWidth / 2, 34, { align: 'center' });

    // Summary boxes
    const summaryY = 44;
    const boxW = 55;
    const gap = 10;
    const startX = (pageWidth - (3 * boxW + 2 * gap)) / 2;
    const summaryItems = [
      { label: 'Total Revenue', value: `\u00A3${totalRevenue.toFixed(2)}` },
      { label: 'Units Sold', value: String(totalQty) },
      { label: 'Products', value: String(items.length) },
    ];
    summaryItems.forEach((s, i) => {
      const x = startX + i * (boxW + gap);
      doc.setDrawColor(0, 48, 135);
      doc.setFillColor(245, 247, 250);
      doc.roundedRect(x, summaryY, boxW, 22, 2, 2, 'FD');
      doc.setFontSize(8);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(120, 120, 120);
      doc.text(s.label.toUpperCase(), x + boxW / 2, summaryY + 8, { align: 'center' });
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(0, 48, 135);
      doc.text(s.value, x + boxW / 2, summaryY + 18, { align: 'center' });
    });
    doc.setTextColor(0, 0, 0);

    // Table
    autoTable(doc, {
      startY: summaryY + 30,
      head: [['Item ID', 'Description', 'Unit Price', 'Qty Sold', 'Revenue']],
      body: items.map(row => [
        row.itemId,
        row.description,
        `\u00A3${row.unitPrice.toFixed(2)}`,
        row.soldPacks,
        `\u00A3${row.total.toFixed(2)}`,
      ]),
      foot: items.length > 0 ? [['', '', 'Totals', totalQty, `\u00A3${totalRevenue.toFixed(2)}`]] : [],
      headStyles: { fillColor: [0, 48, 135], fontSize: 9, fontStyle: 'bold' },
      footStyles: { fillColor: [245, 247, 250], textColor: [0, 48, 135], fontStyle: 'bold', fontSize: 9 },
      bodyStyles: { fontSize: 9 },
      alternateRowStyles: { fillColor: [250, 251, 255] },
      margin: { left: 14, right: 14 },
    });

    doc.save(`IPOS-PU_Sales_Report_${period.from}_to_${period.to}.pdf`);
  };

  const fetchReport = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API}/api/reports/sales?startDate=${period.from}&endDate=${period.to}`);
      const data = await res.json();
      setSalesData(data);
    } catch (err) {
      console.error('Failed to fetch report:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport();
  }, []);

  const items = salesData?.items || [];
  const totalRevenue = salesData?.totalRevenue || 0;
  const totalQty = salesData?.totalPacks || 0;

  return (
    <div className="tab-content">
      <div className="tab-toolbar">
        <h2>Sales Report</h2>
        <div className="period-filter">
          <label>From <input type="date" value={period.from} onChange={e => setPeriod(p => ({ ...p, from: e.target.value }))} /></label>
          <label>To <input type="date" value={period.to} onChange={e => setPeriod(p => ({ ...p, to: e.target.value }))} /></label>
          <button className="btn-secondary" onClick={fetchReport} disabled={loading}>
            {loading ? 'Loading...' : 'Generate'}
          </button>
          {salesData && items.length > 0 && (
            <button className="btn-primary" onClick={downloadPDF}>
              Download PDF
            </button>
          )}
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
          <span className="summary-value">{items.length}</span>
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
            {items.length === 0 ? (
              <tr><td colSpan={5} style={{textAlign:'center', padding:'2rem'}}>No sales data for this period.</td></tr>
            ) : items.map(row => (
              <tr key={row.itemId}>
                <td>{row.itemId}</td>
                <td>{row.description}</td>
                <td>&pound;{row.unitPrice.toFixed(2)}</td>
                <td>{row.soldPacks}</td>
                <td>&pound;{row.total.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
          {items.length > 0 && (
            <tfoot>
              <tr>
                <td colSpan={3} className="tfoot-label">Totals</td>
                <td><strong>{totalQty}</strong></td>
                <td><strong>&pound;{totalRevenue.toFixed(2)}</strong></td>
              </tr>
            </tfoot>
          )}
        </table>
      </div>
    </div>
  );
}

// ── Main Dashboard ─────────────────────────────────────────────────────────────
function MerchantDashboard() {
  const [activeTab, setActiveTab] = useState('promotions');
  const { isLoggedIn, isMerchant, login, logout } = useAuth();
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [loginError, setLoginError] = useState('');
  const [showSignUp, setShowSignUp] = useState(false);
  const [signUpData, setSignUpData] = useState({
    companyName: '', registrationNumber: '', directors: '',
    businessType: '', address: '', email: '', fax: '', preferPhysicalMail: false,
  });
  const [signUpMsg, setSignUpMsg] = useState({ text: '', isError: false });
  const [signUpLoading, setSignUpLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoginError('');

    try {
      const res = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email: credentials.email, password: credentials.password }),
      });
      const data = await res.json();
      if (data.success && data.merchant) {
        login(credentials.email, true);
      } else if (data.success && !data.merchant) {
        setLoginError('This account does not have merchant access.');
      } else {
        setLoginError(data.message || 'Login failed.');
      }
    } catch {
      setLoginError('Could not connect to server.');
    }
  };

  const handleSignUp = async (e) => {
    e.preventDefault();
    setSignUpMsg({ text: '', isError: false });
    setSignUpLoading(true);

    try {
      const res = await fetch(`${API}/merchant/application?email=${encodeURIComponent(signUpData.email)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          companyName: signUpData.companyName,
          companyRegNumber: signUpData.registrationNumber,
          directorName: signUpData.directors,
          businessType: signUpData.businessType,
          address: signUpData.address,
          phone: signUpData.fax,
        }),
      });

      const text = await res.text();
      if (text.startsWith('error:')) {
        setSignUpMsg({ text: text.replace('error: ', ''), isError: true });
      } else {
        setSignUpMsg({ text: 'Membership request submitted successfully! You will be contacted once approved.', isError: false });
        setSignUpData({
          companyName: '', registrationNumber: '', directors: '',
          businessType: '', address: '', email: '', fax: '', preferPhysicalMail: false,
        });
      }
    } catch {
      setSignUpMsg({ text: 'Could not connect to server.', isError: true });
    } finally {
      setSignUpLoading(false);
    }
  };

  if (!isLoggedIn || !isMerchant) {
    return (
      <div className="merchant-login-page">
        <div className={`merchant-login-card ${showSignUp ? 'signup-mode' : ''}`}>
          <div className="ml-logo">IPOS-PU</div>
          <h2>Merchant Portal</h2>

          {!showSignUp ? (
            <>
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

              <p className="ml-toggle">
                Don't have an account?{' '}
                <button className="ml-toggle-btn" onClick={() => { setShowSignUp(true); setLoginError(''); }}>
                  Sign Up
                </button>
              </p>
            </>
          ) : (
            <>
              <p className="ml-subtitle">Request merchant membership to get started.</p>

              {signUpMsg.text && (
                <p className={signUpMsg.isError ? 'ml-error' : 'ml-success'}>{signUpMsg.text}</p>
              )}

              <form onSubmit={handleSignUp} className="ml-form">
                <div className="form-group">
                  <label>Company Name</label>
                  <input
                    type="text"
                    placeholder="e.g. Cosymed Ltd"
                    value={signUpData.companyName}
                    onChange={e => setSignUpData(p => ({ ...p, companyName: e.target.value }))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Registration Number</label>
                  <input
                    type="text"
                    placeholder="e.g. 12345678"
                    value={signUpData.registrationNumber}
                    onChange={e => setSignUpData(p => ({ ...p, registrationNumber: e.target.value }))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Directors</label>
                  <input
                    type="text"
                    placeholder="e.g. John Smith"
                    value={signUpData.directors}
                    onChange={e => setSignUpData(p => ({ ...p, directors: e.target.value }))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Business Type</label>
                  <input
                    type="text"
                    placeholder="e.g. Pharmacy"
                    value={signUpData.businessType}
                    onChange={e => setSignUpData(p => ({ ...p, businessType: e.target.value }))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Address</label>
                  <input
                    type="text"
                    placeholder="e.g. 3 High Level Drive, London"
                    value={signUpData.address}
                    onChange={e => setSignUpData(p => ({ ...p, address: e.target.value }))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input
                    type="email"
                    placeholder="e.g. user@example.com"
                    value={signUpData.email}
                    onChange={e => setSignUpData(p => ({ ...p, email: e.target.value }))}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Fax</label>
                  <input
                    type="text"
                    placeholder="e.g. 0208 778 0124"
                    value={signUpData.fax}
                    onChange={e => setSignUpData(p => ({ ...p, fax: e.target.value }))}
                  />
                </div>
                <div className="form-group form-group-checkbox">
                  <label>
                    <input
                      type="checkbox"
                      checked={signUpData.preferPhysicalMail}
                      onChange={e => setSignUpData(p => ({ ...p, preferPhysicalMail: e.target.checked }))}
                    />
                    Prefer physical mail
                  </label>
                </div>
                <button type="submit" className="ml-submit-btn" disabled={signUpLoading}>
                  {signUpLoading ? 'Submitting...' : 'Submit Request'}
                </button>
              </form>

              <p className="ml-toggle">
                Already have an account?{' '}
                <button className="ml-toggle-btn" onClick={() => { setShowSignUp(false); setSignUpMsg({ text: '', isError: false }); }}>
                  Sign In
                </button>
              </p>
            </>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="merchant-dashboard">
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
            Promotions
          </button>
          <button
            className={`md-nav-item ${activeTab === 'reports' ? 'active' : ''}`}
            onClick={() => setActiveTab('reports')}
          >
            Reports
          </button>
        </nav>
        <button className="md-logout" onClick={() => logout()}>
          Sign Out
        </button>
      </aside>

      <main className="md-main">
        {activeTab === 'promotions' && <PromotionsTab />}
        {activeTab === 'reports' && <ReportsTab />}
      </main>
    </div>
  );
}

export default MerchantDashboard;
