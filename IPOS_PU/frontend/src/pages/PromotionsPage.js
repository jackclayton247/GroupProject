import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import MerchantDashboard from './MerchantDashboard';
import './PromotionsPage.css';

const API = 'http://localhost:8080';

function PublicPromotionsView() {
  const [campaigns, setCampaigns] = useState([]);
  const [selectedCampaign, setSelectedCampaign] = useState(null);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCart();
  const navigate = useNavigate();

  useEffect(() => {
    fetchCampaigns();
  }, []);

  const fetchCampaigns = async () => {
    try {
      const res = await fetch(`${API}/promo/active`);
      const data = await res.json();
      setCampaigns(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch promotions:', err);
      setCampaigns([]);
    } finally {
      setLoading(false);
    }
  };

  const discountedPrice = (price, discount) =>
    parseFloat((price * (1 - discount / 100)).toFixed(2));

  const handleCampaignClick = async (camp) => {
    if (selectedCampaign?.name === camp.name) {
      setSelectedCampaign(null);
      return;
    }
    setSelectedCampaign(camp);
    // Record campaign click
    try {
      await fetch(`${API}/promo/click/campaign`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ campaignName: camp.name }),
      });
    } catch { /* silent */ }
  };

  const handleAddToCart = async (item, campaignName) => {
    addToCart(item.productId, item.description, discountedPrice(item.unitCost, item.discount));
    // Record item click
    try {
      await fetch(`${API}/promo/click/item`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ campaignName, productId: item.productId, quantity: 1 }),
      });
    } catch { /* silent */ }
  };

  if (loading) {
    return (
      <div className="promotions-page">
        <div className="promotions-hero">
          <h1>Active Promotions</h1>
          <p>Loading promotions...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="promotions-page">
      <div className="promotions-hero">
        <h1>Active Promotions</h1>
        <p>Exclusive discounts on selected pharmaceutical goods — limited time only.</p>
      </div>

      <div className="promotions-container">
        {campaigns.length === 0 ? (
          <div style={{textAlign:'center', padding:'3rem', color:'#666'}}>
            <p>No active promotions at the moment. Check back soon!</p>
          </div>
        ) : (
          <div className="campaigns-list">
            {campaigns.map(camp => (
              <div
                key={camp.name}
                className={`campaign-card ${selectedCampaign?.name === camp.name ? 'selected' : ''}`}
                onClick={() => handleCampaignClick(camp)}
              >
                <div className="campaign-badge">ACTIVE</div>
                <h2 className="campaign-name">{camp.name}</h2>
                <p className="campaign-dates">
                  {new Date(camp.startDate).toLocaleDateString('en-GB')} &ndash;{' '}
                  {new Date(camp.endDate).toLocaleDateString('en-GB')}
                </p>
                <p className="campaign-items-count">{camp.items.length} items on promotion</p>
                <button className="campaign-view-btn">
                  {selectedCampaign?.name === camp.name ? 'Hide Items' : 'View Items'}
                </button>
              </div>
            ))}
          </div>
        )}

        {selectedCampaign && (
          <div className="campaign-detail">
            <h2 className="campaign-detail-title">{selectedCampaign.name} — Promoted Items</h2>
            <div className="promo-items-grid">
              {selectedCampaign.items.map(item => (
                <div key={item.productId} className="promo-item-card">
                  <div className="promo-discount-badge">-{item.discount}%</div>
                  <h3 className="promo-item-name">{item.description}</h3>
                  <p className="promo-item-id">ID: {item.itemId}</p>
                  <div className="promo-pricing">
                    <span className="promo-original-price">&pound;{item.unitCost.toFixed(2)}</span>
                    <span className="promo-sale-price">&pound;{discountedPrice(item.unitCost, item.discount).toFixed(2)}</span>
                  </div>
                  <p className="promo-stock">{item.availability} packs available</p>
                  <button
                    className="promo-add-btn"
                    onClick={(e) => { e.stopPropagation(); handleAddToCart(item, selectedCampaign.name); }}
                  >
                    Add to Cart
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function PromotionsPage() {
  const { isMerchant } = useAuth();

  if (isMerchant) {
    return <MerchantDashboard />;
  }

  return <PublicPromotionsView />;
}

export default PromotionsPage;
