import { useLoadingStore } from '@/states';
import loadingSpinner from '@assets/img/loading-spinner.svg';

const LoadingSpinner = () => {
  const { isLoading, message } = useLoadingStore();

  if (!isLoading) return null;

  return (
    <div className="fixed top-0 left-0 w-dvw h-dvh bg-gray-default/[0.5] backdrop-blur-[2px] z-9999">
      <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-transparent text-white flex flex-col items-center">
        <img src={loadingSpinner} alt="loading spinner image" />
        <p className="text-samsung-700-32 mb-[8px]">{message?.title}</p>
        <p className="text-samsung-700-20">{message?.description}</p>
      </div>
    </div>
  );
};

export default LoadingSpinner;
