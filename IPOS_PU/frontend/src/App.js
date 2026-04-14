import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import LoginPage from './pages/LoginPage';
import ProductsPage from './pages/ProductsPage';
import PromotionsPage from './pages/PromotionsPage';
import PurchaseHistoryPage from './pages/PurchaseHistoryPage';
import MerchantDashboard from './pages/MerchantDashboard';
import CartPage from './pages/CartPage';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { useCart } from './context/CartContext';
import './App.css';

const API = 'http://localhost:8080';

function Home() {
  const [bestSellers, setBestSellers] = useState([]);
  const [promotions, setPromotions] = useState([]);
  const [currentPromo, setCurrentPromo] = useState(0);
  const { addToCart } = useCart();

  useEffect(() => {
    fetch(`${API}/api/products`)
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data)) {
          setBestSellers(data.slice(0, 6));
        }
      })
      .catch(err => console.error('Failed to fetch products:', err));

    fetch(`${API}/promo/active`)
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data)) {
          setPromotions(data);
        }
      })
      .catch(err => console.error('Failed to fetch promotions:', err));
  }, []);

  // Cycle through promotions every 4 seconds
  useEffect(() => {
    if (promotions.length <= 1) return;
    const timer = setInterval(() => {
      setCurrentPromo(prev => (prev + 1) % promotions.length);
    }, 4000);
    return () => clearInterval(timer);
  }, [promotions]);

  return (
    <div className="home-page">
      <div className="home-container">
        {/* Best Sellers - Left Side */}
        <section className="best-sellers">
          <h2 className="section-title">Best Sellers</h2>
          <div className="best-sellers-grid">
            {bestSellers.map((item) => (
              <div className="best-seller-card" key={item.productId}>
                <h3 className="best-seller-name">{item.description}</h3>
                <p className="best-seller-price">&pound;{(item.price || 0).toFixed(2)}</p>
                <button className="add-to-cart-btn" onClick={() => addToCart(item)}>Add to Cart</button>
              </div>
            ))}
          </div>
          <Link to="/products" className="view-all-link">View All Products &rarr;</Link>
        </section>

        {/* Promotional Banner - Right Side */}
        <aside className="promo-sidebar">
          {promotions.length > 0 ? (
            <div className="promo-banner promo-main">
              <div className="promo-badge">SALE</div>
              <h2>{promotions[currentPromo].name}</h2>
              <p>Up to <strong>{Math.max(...promotions[currentPromo].items.map(i => i.discount))}% OFF</strong> on {promotions[currentPromo].items.length} item{promotions[currentPromo].items.length !== 1 ? 's' : ''}</p>
              <p style={{fontSize: '0.85rem', opacity: 0.8}}>Ends {new Date(promotions[currentPromo].endDate).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}</p>
              {promotions.length > 1 && (
                <div style={{display: 'flex', justifyContent: 'center', gap: '6px', margin: '8px 0'}}>
                  {promotions.map((_, i) => (
                    <span key={i} onClick={() => setCurrentPromo(i)} style={{width: 8, height: 8, borderRadius: '50%', background: i === currentPromo ? '#fff' : 'rgba(255,255,255,0.4)', cursor: 'pointer'}} />
                  ))}
                </div>
              )}
              <Link to="/promotions" className="promo-cta">View Promotions</Link>
            </div>
          ) : (
            <div className="promo-banner promo-main">
              <div className="promo-badge">SHOP</div>
              <h2>Great Deals</h2>
              <p>Check out our latest products</p>
              <Link to="/products" className="promo-cta">Browse Products</Link>
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Router>
          <div className="App">
            <Header />
            <main>
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/products" element={<ProductsPage />} />
                <Route path="/promotions" element={<PromotionsPage />} />
                <Route path="/purchase-history" element={<PurchaseHistoryPage />} />
                <Route path="/merchant" element={<MerchantDashboard />} />
                <Route path="/cart" element={<CartPage />} />
              </Routes>
            </main>
          </div>
        </Router>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;
