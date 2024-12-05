import { useState } from 'react';

// const apiUrl = import.meta.env.VITE_API_URL;

const SignInProxy = () => {
  const [hi, setHi] = useState(1);

  const clickFunc = async () => {
    console.log('hi:', hi);
    setHi((prev) => prev + 1);
    // console.log('GIGYA_API_KEY:', window.serverData.GIGYA_API_KEY);
    //
    // const response = await axios.post(`${apiUrl}/new-consent/consentVersionCheck`);
    // console.log(response);
    //
    // return response;
  };

  return (
    <>
      <div>login-proxy</div>
      <button type="button" onClick={clickFunc}>
        button
      </button>
      <div>{hi}</div>
      {hi > 5 && <div>ok</div>}
    </>
  );
};

export default SignInProxy;
