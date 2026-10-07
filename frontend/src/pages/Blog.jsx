import { Link } from "react-router-dom";
import { ARTICLES } from "../data/articles.js";
export default function Blog() {
  return (
    <>
      <h1 className="mb-4">Blog SportX</h1>
      <div className="row g-4">
        {ARTICLES.map((a) => (
          <article key={a.id} className="col-md-6">
            <div className="card h-100 p-4">
              <small className="text-danger">{a.category}</small>
              <h2 className="h4 mt-2">{a.title}</h2>
              <p>{a.intro}</p>
              <Link to={"/blogs/" + a.id}>Leer artículo</Link>
            </div>
          </article>
        ))}
      </div>
    </>
  );
}
