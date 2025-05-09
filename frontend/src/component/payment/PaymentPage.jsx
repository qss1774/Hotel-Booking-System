import { Elements } from "@stripe/react-stripe-js";
import { loadStripe } from "@stripe/stripe-js";
import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import PaymentForm from "./PaymentForm";
import ApiService from "../../service/ApiService";


const PaymentPage = () => {
    const {bookingReference, amount} = useParams()
    const [clientSecret, setClientSecret] = useState(null)
    const [error, setError] = useState(null)
    const [paymentStatus, setPaymentStatus] = useState(null)
    const navigate = useNavigate();

    useEffect(()=> {
        const fetchClientSecrete = async () => {
            try {
                const paymentData = {bookingReference, amount};
                console.log("BOOKING NO IS: " + bookingReference)
                console.log("Amount  IS: " + amount)

                const uniquePaymentSecret = await ApiService.proceedForPayment(paymentData);

                console.log("UNIQUE CLIENT SECRETE FROM fetchClientSecrete is: " + uniquePaymentSecret);
                setClientSecret(uniquePaymentSecret);
                
            } catch (error) {
                console.log(error)
                setError(error.response?.data?.message || error.message)
            }
        };
        fetchClientSecrete();
    }, [bookingReference, amount])



    if (error) {
        return <div className="error-message">{error}</div>
    }

    //initilize strip with public key

    const stripePromise = loadStripe(
        "pk_test_51RJ6rlGdrfOLTdiLJ4h1zAl2Cc1J3UHCvu96zaZ7lHl0WQ9LzDtt91hQCfPDY9ZBNmeJMvWf5pzATKoNbpTHnreY00azAFAK46"
    );

    //update payment status for our booking in our backend databse
    const handlePaymentStatus =  async (paymentStatus, transactionId = "", failureReason = "") => {
        try {

            const paymentData = {
                bookingReference,
                amount,
                transactionId,
                success: paymentStatus === "succeeded",
                failureReason
            }
            
            await ApiService.updateBookingPayment(paymentData)
            console.log("Payment status was updated")
        } catch (error) {
            console.log(error.message)
        }
    }

    return(
        <div className="payment-page">
            <Elements stripe={stripePromise} options={clientSecret}>
                <PaymentForm
                clientSecrete={clientSecret}
                amount={amount}
                onPaymentSuccess={(transactionId) => {
                    setPaymentStatus("succeeded")
                    handlePaymentStatus("succeeded", transactionId)
                    navigate(`/payment-success/${bookingReference}`)
                }}
                onPaymentError={(error) => {
                    setPaymentStatus("failed");
                    handlePaymentStatus("failed", "", error.message)
                    navigate(`/payment-failed/${bookingReference}`);

                }}
                
                />
            </Elements>

            {paymentStatus && <div>Payment Status: {paymentStatus}</div>}
        </div>
    )

}

export default PaymentPage;