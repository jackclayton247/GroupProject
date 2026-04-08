import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import MerchantDashboard from './MerchantDashboard';
import './PromotionsPage.css';

const mockCampaigns = [
  {
    id: 'CAMP_001',
    name: 'Spring Sale',
    startDate: '2026-04-01',
    endDate: '2026-04-30',
    items: [
      { productId: 1, itemId: '100 00001', description: 'Paracetamol', unitCost: 0.10, discount: 20, availability: 500 },
      { productId: 2, itemId: '100 00002', description: 'Aspirin', unitCost: 0.50, discount: 15, availability: 300 },
      { productId: 3, itemId: '400 00001', description: 'Vitamin C', unitCost: 1.20, discount: 10, availability: 200 },
    ],
  },
  {
    id: 'CAMP_002',
    name: 'Wellness Week',
    startDate: '2026-04-08',
    endDate: '2026-04-15',
    items: [
      { productId: 4, itemId: '400 00002', description: 'Vitamin B12', unitCost: 1.30, discount: 25, availability: 150 },
      { productId: 5, itemId: '200 00005', description: 'Rhynol', unitCost: 2.50, discount: 10, availability: 180 },
    ],
  },
];

function PublicPromotionsView() {
  const [selectedCampaign, setSelectedCampaign] = useState(null);
  const { addToCart } = useCart();
  const navigate = useNavigate();

  const discountedPrice = (price, discount) =>
    parseFloat((price * (1 - discount / 100)).toFixed(2));

  const handleAddToCart = (item) => {
    addToCart(item.productId, item.description, discountedPrice(item.unitCost, item.discount));
  };

  return (
    <div className="promotions-page">
      <div className="promotions-hero">
        <h1>Active Promotions</h1>
        <p>Exclusive discounts on selected pharmaceutical goods — limited time only.</p>
      </div>

      <div className="promotions-container">
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
                    <span className="promo-sale-price">&pound;{discountedPrice(item.unitCost, item.discount).toFixed(2)}</span>
                  </div>
                  <p className="promo-stock">{item.availability} packs available</p>
                  <button
                    className="promo-add-btn"
                    onClick={() => handleAddToCart(item)}
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
