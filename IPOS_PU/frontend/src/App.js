import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Header from './components/Header';
import LoginPage from './pages/LoginPage';
import ProductsPage from './pages/ProductsPage';
import './App.css';

const bestSellers = [
  { id: 1, name: 'Nurofen', price: 1.89, image: null },
  { id: 2, name: 'Cough Syrup', price: 2.49, image: null },
  { id: 3, name: 'Ozempic', price: 3.15, image: null },
  { id: 4, name: 'Purple Lean', price: 4.50, image: null },
  { id: 5, name: 'Xanax', price: 1.99, image: null },
  { id: 6, name: 'Percs', price: 4.25, image: null },
];

function Home() {
  return (
    <div className="home-page">
      <div className="home-container">
        {/* Best Sellers - Left Side */}
        <section className="best-sellers">
          <h2 className="section-title">Best Sellers</h2>
          <div className="best-sellers-grid">
            {bestSellers.map((item) => (
              <div className="best-seller-card" key={item.id}>
<h3 className="best-seller-name">{item.name}</h3>
                <p className="best-seller-price">&pound;{item.price.toFixed(2)}</p>
                <button className="add-to-cart-btn">Add to Cart</button>
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
            <Link to="/products" className="promo-cta">Shop Now</Link>
          </div>
        </aside>
      </div>
    </div>
  );
}

function App() {
  return (
    <Router>
      <div className="App">
        <Header />
        <main>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/products" element={<ProductsPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
