import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Header.css';

function Header() {
  const location = useLocation();
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <header className="header">
      {/* Top bar */}
      <div className="header-top">
        <div className="header-container">
          <Link to="/" className="header-logo">IPOS-PU</Link>
          <div className="header-search">
            <input
              type="text"
              placeholder="Search products, categories..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button className="search-btn" aria-label="Search">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
            </button>
          </div>
          <div className="header-actions">
            <Link to="/login" className="commercial-btn">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
              </svg>
              Apply for Commercial Membership
            </Link>
          </div>
        </div>
      </div>

      {/* Navigation bar */}
      <nav className="header-nav">
        <div className="header-container">
          <ul className="nav-links">
            <li><Link to="/" className={location.pathname === '/' ? 'active' : ''}>Home</Link></li>
            <li><Link to="/products" className={location.pathname === '/products' ? 'active' : ''}>Products</Link></li>
            <li><Link to="/promotions" className={location.pathname === '/promotions' ? 'active' : ''}>Promotions</Link></li>
            <li><Link to="/track-order" className={location.pathname === '/track-order' ? 'active' : ''}>Track Order</Link></li>
            <li><Link to="/login" className={location.pathname === '/login' ? 'active' : ''}>Admin / Login</Link></li>
          </ul>
          <div className="nav-right">
            <Link to="/login" className="nav-login-btn">Admin/Login</Link>
            <Link to="/cart" className="cart-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
              Cart
            </Link>
          </div>
        </div>
      </nav>
    </header>
  );
}

export default Header;
