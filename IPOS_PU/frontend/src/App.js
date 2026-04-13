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
  }, []);

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
          <div className="promo-banner promo-main">
            <div className="promo-badge">SALE</div>
            <h2>Spring Sale</h2>
            <p>Up to <strong>30% OFF</strong> on selected items</p>
            <Link to="/promotions" className="promo-cta">View Promotions</Link>
          </div>
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
