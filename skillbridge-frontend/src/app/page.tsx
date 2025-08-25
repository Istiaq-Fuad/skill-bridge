export default function Home() {
  const data = fetch("http://localhost:8080/jobs");

  return (
    <div className="font-sans grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20">
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
}
