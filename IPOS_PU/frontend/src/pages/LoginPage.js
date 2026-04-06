import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';

function LoginPage() {
  const navigate = useNavigate();
  const [isRegister, setIsRegister] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (isRegister && formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    const endpoint = isRegister ? '/auth/signup' : '/auth/login';

    try {
      const response = await fetch(`http://localhost:8080${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: formData.email,
          password: formData.password,
        }),
      });

      const text = await response.text();

      if (isRegister) {
        if (text === 'account created') {
          setSuccess('Account created! You can now sign in.');
          setIsRegister(false);
          setFormData({ email: formData.email, password: '', confirmPassword: '', firstName: '', lastName: '' });
        } else {
          setError(text);
        }
      } else {
        if (text === 'session created') {
          navigate('/');
        } else {
          setError(text);
        }
      }
    } catch (err) {
      setError('Could not connect to the server. Make sure the backend is running.');
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-header">
          <h2>{isRegister ? 'Create Account' : 'Sign In'}</h2>
        </div>

        {error && <p className="login-error">{error}</p>}
        {success && <p className="login-success">{success}</p>}

        <form onSubmit={handleSubmit} className="login-form">
          {isRegister && (
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="firstName">First Name</label>
                <input
                  type="text"
                  id="firstName"
                  name="firstName"
                  placeholder="John"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="lastName">Last Name</label>
                <input
                  type="text"
                  id="lastName"
                  name="lastName"
                  placeholder="Doe"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              name="email"
              placeholder="you@example.com"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          {isRegister && (
            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                placeholder="Confirm your password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>
          )}

          <button type="submit" className="login-submit-btn">
            {isRegister ? 'Create Account' : 'Sign In'}
          </button>
        </form>

        <div className="login-divider">
          <span>or</span>
        </div>

        <div className="login-switch">
          {isRegister ? (
            <p>Already have an account? <button onClick={() => setIsRegister(false)} className="switch-btn">Sign In</button></p>
          ) : (
            <p>Don't have an account? <button onClick={() => setIsRegister(true)} className="switch-btn">Create Account</button></p>
          )}
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
