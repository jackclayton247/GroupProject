import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './PromotionDetailPage.css';

function PromotionDetailPage() {
  const { name } = useParams();
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await fetch(`http://localhost:8080/promo/products?name=${encodeURIComponent(name)}`);
        if (response.ok) {
          const data = await response.json();
          setProducts(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        console.error('Error fetching promotion products:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [name]);

  const handleRemoveProduct = async (productId) => {
    try {
      const response = await fetch(`http://localhost:8080/promo/removeProduct?productId=${productId}`, {
        method: 'POST'
      });
      if (response.ok) {
        setProducts(products.filter(p => p.productId !== productId));
      }
    } catch (err) {
      console.error('Error removing product:', err);
    }
  };

  const handleAddProducts = () => {
    navigate(`/products?addToPromotion=${encodeURIComponent(name)}`);
  };

  if (loading) {
    return (
      <div className="promotion-detail-page">
        <div className="promotion-detail-container">
          <h1>{decodeURIComponent(name)}</h1>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="promotion-detail-page">
      <div className="promotion-detail-container">
        <button className="back-btn" onClick={() => navigate('/promotions')}>
          ← Back to Promotions
        </button>
        
        <h1>{decodeURIComponent(name)}</h1>
        
        {products.length === 0 ? (
          <p className="no-products">No products in this promotion yet.</p>
        ) : (
          <div className="products-row">
            {products.map((product) => (
              <div key={product.productId} className="product-card">
                <h3 className="product-name">{product.productName}</h3>
                <p className="product-discount">{product.discount}% off</p>
                <button 
                  className="remove-btn"
                  onClick={() => handleRemoveProduct(product.productId)}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}

        <button className="add-products-btn" onClick={handleAddProducts}>
          + Add Products
        </button>
      </div>
    </div>
  );
}

export default PromotionDetailPage;
