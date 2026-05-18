import React, { useState, useEffect } from 'react';

export function CountdownTimer({ endTime }) {
    const [timeLeft, setTimeLeft] = useState('');

    useEffect(() => {
        const calculateTimeLeft = () => {
            const targetDate = new Date(endTime);
            const now = new Date();
            const difference = targetDate - now;

            if (difference <= 0) {
                return "Auction Ended";
            }

            const days = Math.floor(difference / (1000 * 60 * 60 * 24));
            const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
            const minutes = Math.floor((difference / 1000 / 60) % 60);
            const seconds = Math.floor((difference / 1000) % 60);

            let timeString = "";
            if (days > 0) timeString += `${days}d `;
            timeString += `${hours}h ${minutes}m ${seconds}s`;

            return timeString;
        };

        setTimeLeft(calculateTimeLeft());

        const timer = setInterval(() => {
            setTimeLeft(calculateTimeLeft());
        }, 1000);

        return () => clearInterval(timer);
    }, [endTime]);

    return (
        <div style={{
            display: 'inline-block',
            backgroundColor: '#fef3c7',
            color: '#b45309',
            padding: '0.4rem 0.8rem',
            borderRadius: '6px',
            fontWeight: 'bold',
            fontSize: '0.9rem',
            marginLeft: '1rem'
        }}>
            Ends in: {timeLeft}
        </div>
    );
}