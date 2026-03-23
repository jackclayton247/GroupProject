import React, { useState } from 'react';
import './ProductsPage.css';

const categories = [
  { id: "100", name: "Category 1" },
  { id: "200", name: "Category 2" },
  { id: "300", name: "Category 3" },
  { id: "400", name: "Category 4" },
];

function ProductsPage() {
    const [selectedCategory, setSelectedCategory] = useState('100');
  return (
    <div className="products-page">
        <div className="products-sidebar">
            <h2 className="sidebar-title">Categories</h2>
            <ul className="category-list">
                {categories.map(c => (
                    <li
                    key={c.id}
                    className={"category-item " + (selectedCategory === c.id ? "active" : "")}
                    onClick={() => setSelectedCategory(c.id)}>
                        {c.name}
                    </li>
                ))}
            </ul>
        </div>
        <div className="products-main">
            <h2 className="products-main-title">{categories.find(c => c.id === selectedCategory)?.name}</h2>
        </div>
    </div>
  );
}

export default ProductsPage;