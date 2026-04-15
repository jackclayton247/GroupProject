import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './CartPage.css';

const CARD_TYPES = ['VISA', 'Mastercard', 'Amex'];

function CartPage() {
  const { cart, removeFromCart, updateQuantity, clearCart, totalPrice } = useCart();
  const { isLoggedIn, userEmail } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    deliveryAddress: '',
    cardType: 'VISA',
    cardFirstFour: '',
    cardLastFour: '',
    cardExpiry: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!isLoggedIn) {
      setError('You must be signed in to place an order.');
      return;
    }

    if (!userEmail) {
      setError('User email not found. Please log in again.');
      return;
    }

    if (cart.length === 0) {
      setError('Your cart is empty.');
      return;
    }

    // Validation
    if (!form.deliveryAddress.trim()) {
      setError('Please enter a delivery address.');
      return;
    }
    if (!form.cardFirstFour || form.cardFirstFour.length !== 4) {
      setError('Please enter the first 4 digits of your card.');
      return;
    }
    if (!form.cardLastFour || form.cardLastFour.length !== 4) {
      setError('Please enter the last 4 digits of your card.');
      return;
    }
    console.log('DEBUG cardExpiry value:', JSON.stringify(form.cardExpiry));
    if (!form.cardExpiry || !form.cardExpiry.match(/^\d{2}.?\d{2}$/)) {
      setError(`Please enter expiry date in format MM/YY. Got: "${form.cardExpiry}"`);
      return;
    }

    const payload = {
      userEmail: userEmail,
      deliveryAddress: form.deliveryAddress,
      cardType: form.cardType,
      cardFirstFour: form.cardFirstFour,
      cardLastFour: form.cardLastFour,
      cardExpiry: form.cardExpiry,
      items: cart.map(i => ({ productId: i.productId, quantity: i.quantity })),
    };

    console.log('DEBUG - userEmail:', userEmail);
    console.log('DEBUG - isLoggedIn:', isLoggedIn);
    console.log('DEBUG - cart:', cart);
    console.log('DEBUG - Submitting order payload:', JSON.stringify(payload, null, 2));

    setSubmitting(true);
    try {
      const res = await fetch('http://localhost:8080/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const responseText = await res.text();
      console.log('Response:', res.status, responseText);

      if (res.ok) {
        clearCart();
        setSuccess('Order placed successfully! Check your email for confirmation.');
        setForm({ deliveryAddress: '', cardType: 'VISA', cardFirstFour: '', cardLastFour: '', cardExpiry: '' });
      } else {
        setError(responseText || `Failed to place order (HTTP ${res.status}). Please try again.`);
      }
    } catch (err) {
      console.error('Order error:', err);
      setError('Could not connect to the server. Make sure the backend is running.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="cart-page">
      {/* Left — Cart Items */}
      <section className="cart-section cart-left">
        <h2 className="cart-heading">Shopping Cart</h2>
        {cart.length === 0 ? (
          <div className="cart-empty">
            <p>Your cart is empty.</p>
            <button className="cart-btn-primary" onClick={() => navigate('/products')}>Browse Products</button>
          </div>
        ) : (
          <>
            <div className="cart-items-list">
              {cart.map(item => (
                <div key={item.productId} className="cart-item">
                  <div className="cart-item-info">
                    <h3 className="cart-item-name">{item.description}</h3>
                    <p className="cart-item-price">&pound;{(item.price || 0).toFixed(2)} each</p>
                  </div>
                  <div className="cart-item-controls">
                    <button className="qty-btn" onClick={() => updateQuantity(item.productId, item.quantity - 1)}>−</button>
                    <span className="qty-value">{item.quantity}</span>
                    <button className="qty-btn" onClick={() => updateQuantity(item.productId, item.quantity + 1)}>+</button>
                  </div>
                  <div className="cart-item-subtotal">
                    &pound;{((item.price || 0) * item.quantity).toFixed(2)}
                  </div>
                  <button className="cart-item-remove" onClick={() => removeFromCart(item.productId)} aria-label="Remove">&#215;</button>
                </div>
              ))}
            </div>

            <div className="cart-total">
              <span>Total</span>
              <span>&pound;{totalPrice.toFixed(2)}</span>
            </div>
          </>
        )}
      </section>

      {/* Right — Checkout Form */}
      <section className="cart-section cart-right">
        <h2 className="cart-heading">Checkout</h2>

        {error && <div className="cart-error" style={{whiteSpace: 'pre-wrap', wordBreak: 'break-word'}}>{error}</div>}
        {success && <div className="cart-success">{success}</div>}

        <form onSubmit={handleSubmit} className="checkout-form">
          <label className="form-label">Delivery Address</label>
          <textarea
            name="deliveryAddress"
            className="form-input"
            rows="3"
            value={form.deliveryAddress}
            onChange={handleChange}
            required
          />

          <h3 className="form-subheading">Payment Details</h3>

          <label className="form-label">Card Type</label>
          <select name="cardType" className="form-input" value={form.cardType} onChange={handleChange}>
            {CARD_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
          </select>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">First 4 Digits</label>
              <input
                name="cardFirstFour"
                className="form-input"
                maxLength="4"
                value={form.cardFirstFour}
                onChange={handleChange}
                placeholder="1234"
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Last 4 Digits</label>
              <input
                name="cardLastFour"
                className="form-input"
                maxLength="4"
                value={form.cardLastFour}
                onChange={handleChange}
                placeholder="5678"
                required
              />
            </div>
          </div>

          <label className="form-label">Expiry Date (MM/YY)</label>
          <input
            name="cardExpiry"
            className="form-input"
            placeholder="MM/YY"
            value={form.cardExpiry}
            onChange={handleChange}
            required
          />

          <div className="cart-summary">
            <span>{cart.reduce((sum, i) => sum + i.quantity, 0)} item(s)</span>
            <span className="cart-summary-total">&pound;{totalPrice.toFixed(2)}</span>
          </div>

          <button type="submit" className="cart-btn-primary" disabled={submitting || cart.length === 0}>
            {submitting ? 'Placing Order...' : 'Place Order'}
          </button>
        </form>
      </section>
    </div>
  );
}

export default CartPage;
