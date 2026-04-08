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

    if (cart.length === 0) {
      setError('Your cart is empty.');
      return;
    }

    const payload = {
      userEmail,
      deliveryAddress: form.deliveryAddress,
      cardType: form.cardType,
      cardFirstFour: form.cardFirstFour,
      cardLastFour: form.cardLastFour,
      cardExpiry: form.cardExpiry,
      items: cart.map(i => ({ productId: i.productId, quantity: i.quantity })),
    };

    setSubmitting(true);
    try {
      const res = await fetch('http://localhost:8080/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        clearCart();
        setSuccess('Order placed successfully! Check your email for confirmation.');
        setForm({ deliveryAddress: '', cardType: 'VISA', cardFirstFour: '', cardLastFour: '', cardExpiry: '' });
      } else {
        const text = await res.text();
        setError(text || 'Failed to place order. Please try again.');
      }
    } catch {
      setError('Could not connect to the server. Make sure the backend is running.');
    } finally {
      setSubmitting(false);
    }
  };

  if (success) {
    return (
      <div className="cart-page">
        <div className="cart-success">
          <div className="cart-success-icon">&#10003;</div>
          <h2>Order Placed!</h2>
          <p>{success}</p>
          <div className="cart-success-actions">
            <button className="cart-btn-primary" onClick={() => navigate('/purchase-history')}>View Orders</button>
            <button className="cart-btn-secondary" onClick={() => navigate('/products')}>Continue Shopping</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <div className="cart-container">
        {/* Left — Cart Items */}
        <section className="cart-items-section">
          <h1 className="cart-title">Your Cart</h1>

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
                      <p className="cart-item-price">&pound;{item.price.toFixed(2)} each</p>
                    </div>
                    <div className="cart-item-controls">
                      <button className="qty-btn" onClick={() => updateQuantity(item.productId, item.quantity - 1)}>−</button>
                      <span className="qty-value">{item.quantity}</span>
                      <button className="qty-btn" onClick={() => updateQuantity(item.productId, item.quantity + 1)}>+</button>
                    </div>
                    <div className="cart-item-subtotal">
                      &pound;{(item.price * item.quantity).toFixed(2)}
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
        {cart.length > 0 && (
          <section className="checkout-section">
            <h2 className="checkout-title">Checkout</h2>

            {!isLoggedIn && (
              <div className="checkout-login-prompt">
                Please <button className="checkout-login-link" onClick={() => navigate('/login')}>sign in</button> to place your order.
              </div>
            )}

            {error && <p className="checkout-error">{error}</p>}

            <form className="checkout-form" onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="deliveryAddress">Delivery Address</label>
                <textarea
                  id="deliveryAddress"
                  name="deliveryAddress"
                  placeholder="123 Example Street, London, EC1A 1BB"
                  value={form.deliveryAddress}
                  onChange={handleChange}
                  rows={3}
                  required
                />
              </div>

              <div className="checkout-section-label">Payment Details</div>

              <div className="form-group">
                <label htmlFor="cardType">Card Type</label>
                <select id="cardType" name="cardType" value={form.cardType} onChange={handleChange}>
                  {CARD_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label htmlFor="cardFirstFour">First 4 Digits</label>
                  <input
                    id="cardFirstFour"
                    name="cardFirstFour"
                    type="text"
                    inputMode="numeric"
                    maxLength={4}
                    placeholder="0000"
                    value={form.cardFirstFour}
                    onChange={handleChange}
                    pattern="\d{4}"
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="cardLastFour">Last 4 Digits</label>
                  <input
                    id="cardLastFour"
                    name="cardLastFour"
                    type="text"
                    inputMode="numeric"
                    maxLength={4}
                    placeholder="1111"
                    value={form.cardLastFour}
                    onChange={handleChange}
                    pattern="\d{4}"
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="cardExpiry">Expiry Date</label>
                <input
                  id="cardExpiry"
                  name="cardExpiry"
                  type="text"
                  placeholder="MM/YY"
                  maxLength={5}
                  value={form.cardExpiry}
                  onChange={handleChange}
                  pattern="\d{2}/\d{2}"
                  required
                />
              </div>

              <div className="checkout-summary">
                <span>{cart.reduce((s, i) => s + i.quantity, 0)} item(s)</span>
                <span className="checkout-total">&pound;{totalPrice.toFixed(2)}</span>
              </div>

              <button
                type="submit"
                className="checkout-submit-btn"
                disabled={submitting || !isLoggedIn}
              >
                {submitting ? 'Placing Order…' : 'Place Order'}
              </button>
            </form>
          </section>
        )}
      </div>
    </div>
  );
}

export default CartPage;
