export default function AuthCard({ title, children }) {
    return (
      <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl p-8">
        <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">
          {title}
        </h2>
        {children}
      </div>
    );
}