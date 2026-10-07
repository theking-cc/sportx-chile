import { useParams, Link } from "react-router-dom";
import { ARTICLES } from "../data/articles.js";
export default function Article() {
  const { id } = useParams();
  const article = ARTICLES.find((a) => a.id === id);
  if (!article)
    return (
      <>
        <h1>Artículo no encontrado</h1>
        <Link to="/blogs">Volver al blog</Link>
      </>
    );
  return (
    <article className="card p-4">
      <p className="text-danger">{article.category}</p>
      <h1>{article.title}</h1>
      <p className="lead">{article.intro}</p>
      {article.paragraphs.map((p, i) => (
        <p key={i}>{p}</p>
      ))}
      <Link to="/blogs">Volver al blog</Link>
    </article>
  );
}
