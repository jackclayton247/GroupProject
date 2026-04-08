import React, { useState } from 'react';
import './PromotionsPage.css';

const mockCampaigns = [
  {
    id: 'CAMP_001',
    name: 'Spring Sale',
    startDate: '2026-04-01',
    endDate: '2026-04-30',
    items: [
      { itemId: '100 00001', description: 'Paracetamol', unitCost: 0.10, discount: 20, availability: 500 },
      { itemId: '100 00002', description: 'Aspirin', unitCost: 0.50, discount: 15, availability: 300 },
      { itemId: '400 00001', description: 'Vitamin C', unitCost: 1.20, discount: 10, availability: 200 },
    ],
  },
  {
    id: 'CAMP_002',
    name: 'Wellness Week',
    startDate: '2026-04-08',
    endDate: '2026-04-15',
    items: [
      { itemId: '400 00002', description: 'Vitamin B12', unitCost: 1.30, discount: 25, availability: 150 },
      { itemId: '200 00005', description: 'Rhynol', unitCost: 2.50, discount: 10, availability: 180 },
    ],
  },
];

function PromotionsPage() {
  const [selectedCampaign, setSelectedCampaign] = useState(null);
  const [cart, setCart] = useState({});

  const addToCart = (itemId, description) => {
    setCart(prev => ({
      ...prev,
      [itemId]: { description, qty: (prev[itemId]?.qty || 0) + 1 },
    }));
  };

  const discountedPrice = (price, discount) =>
    (price * (1 - discount / 100)).toFixed(2);

  return (
    <div className="promotions-page">
      <div className="promotions-hero">
        <h1>Active Promotions</h1>
        <p>Exclusive discounts on selected pharmaceutical goods — limited time only.</p>
      </div>

      <div className="promotions-container">
        {/* Campaign List */}
        <div className="campaigns-list">
          {mockCampaigns.map(camp => (
            <div
              key={camp.id}
              className={`campaign-card ${selectedCampaign?.id === camp.id ? 'selected' : ''}`}
              onClick={() => setSelectedCampaign(selectedCampaign?.id === camp.id ? null : camp)}
            >
              <div className="campaign-badge">ACTIVE</div>
              <h2 className="campaign-name">{camp.name}</h2>
              <p className="campaign-dates">
                {new Date(camp.startDate).toLocaleDateString('en-GB')} &ndash;{' '}
                {new Date(camp.endDate).toLocaleDateString('en-GB')}
              </p>
              <p className="campaign-items-count">{camp.items.length} items on promotion</p>
              <button className="campaign-view-btn">
                {selectedCampaign?.id === camp.id ? 'Hide Items' : 'View Items'}
              </button>
            </div>
          ))}
        </div>

        {/* Campaign Items */}
        {selectedCampaign && (
          <div className="campaign-detail">
            <h2 className="campaign-detail-title">{selectedCampaign.name} — Promoted Items</h2>
            <div className="promo-items-grid">
              {selectedCampaign.items.map(item => (
                <div key={item.itemId} className="promo-item-card">
                  <div className="promo-discount-badge">-{item.discount}%</div>
                  <h3 className="promo-item-name">{item.description}</h3>
                  <p className="promo-item-id">ID: {item.itemId}</p>
                  <div className="promo-pricing">
                    <span className="promo-original-price">&pound;{item.unitCost.toFixed(2)}</span>
                    <span className="promo-sale-price">&pound;{discountedPrice(item.unitCost, item.discount)}</span>
                  </div>
                  <p className="promo-stock">{item.availability} packs available</p>
                  <button
                    className="promo-add-btn"
                    onClick={() => addToCart(item.itemId, item.description)}
                  >
                    Add to Cart
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Mini Cart Summary */}
        {Object.keys(cart).length > 0 && (
          <div className="promo-cart-summary">
            <h3>Cart</h3>
            <ul>
              {Object.entries(cart).map(([id, { description, qty }]) => (
                <li key={id}>{description} &times; {qty}</li>
              ))}
            </ul>
            <button className="promo-checkout-btn">Proceed to Checkout</button>
          </div>
        )}
      </div>
    </div>
  );
}

export default PromotionsPage;
