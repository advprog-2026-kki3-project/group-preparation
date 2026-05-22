import React, { useEffect, useState } from 'react';
import {
    fetchNotificationPreferences,
    fetchNotifications,
    markNotificationRead,
    updateNotificationPreferences
} from './notificationApi';

const typeLabel = (type) => type.replaceAll('_', ' ');

export function NotificationsPage({ currentUser }) {
    const [notifications, setNotifications] = useState([]);
    const [emailEnabled, setEmailEnabled] = useState(true);
    const [pushEnabled, setPushEnabled] = useState(true);
    const [error, setError] = useState('');
    const [preferenceError, setPreferenceError] = useState('');

    const loadNotifications = async () => {
        setError('');
        try {
            setNotifications(await fetchNotifications(currentUser.principal));
        } catch (loadError) {
            setError(loadError.message);
        }
    };

    useEffect(() => {
        loadNotifications();
        const intervalId = setInterval(loadNotifications, 5000);
        return () => clearInterval(intervalId);
    }, [currentUser.principal]);

    useEffect(() => {
        const loadPreferences = async () => {
            setPreferenceError('');
            try {
                const preferences = await fetchNotificationPreferences(currentUser.principal);
                setEmailEnabled(preferences.emailEnabled);
                setPushEnabled(preferences.pushEnabled);
            } catch (loadError) {
                setPreferenceError(loadError.message);
            }
        };

        loadPreferences();
    }, [currentUser.principal]);

    const savePreferences = async (nextPreferences) => {
        setPreferenceError('');
        try {
            const savedPreferences = await updateNotificationPreferences(currentUser.principal, nextPreferences);
            setEmailEnabled(savedPreferences.emailEnabled);
            setPushEnabled(savedPreferences.pushEnabled);
        } catch (saveError) {
            setPreferenceError(saveError.message);
        }
    };

    const updateEmailPreference = (enabled) => {
        setEmailEnabled(enabled);
        savePreferences({ emailEnabled: enabled, pushEnabled });
    };

    const updatePushPreference = (enabled) => {
        setPushEnabled(enabled);
        savePreferences({ emailEnabled, pushEnabled: enabled });
    };

    const markRead = async (notificationId) => {
        await markNotificationRead(notificationId);
        await loadNotifications();
    };

    return (
        <div className="grid two">
            <section className="panel">
                <div className="section-heading">
                    <h2>Notifications</h2>
                    <button className="secondary" onClick={loadNotifications}>Refresh</button>
                </div>

                {error && <div className="message error">{error}</div>}

                <div className="list">
                    {notifications.length === 0 ? (
                        <div className="empty-state">No notifications yet.</div>
                    ) : (
                        notifications.map((notification) => (
                            <article
                                key={notification.id}
                                className="list-item"
                                style={{ backgroundColor: notification.read ? '#ffffff' : '#f8fafc' }}
                            >
                                <div>
                                    <p>
                                        <strong>{typeLabel(notification.type)}</strong>
                                    </p>
                                    <small>{notification.message}</small>
                                    <small style={{ display: 'block', marginTop: '4px' }}>
                                        {new Date(notification.createdAt).toLocaleString()}
                                    </small>
                                </div>
                                {!notification.read && (
                                    <button className="secondary" onClick={() => markRead(notification.id)}>
                                        Mark Read
                                    </button>
                                )}
                            </article>
                        ))
                    )}
                </div>
            </section>

            <section className="panel">
                <h2>Preferences</h2>
                {preferenceError && <div className="message error">{preferenceError}</div>}
                <label className="row" style={{ alignItems: 'center' }}>
                    <input
                        type="checkbox"
                        checked={emailEnabled}
                        onChange={(event) => updateEmailPreference(event.target.checked)}
                        style={{ width: 'auto' }}
                    />
                    Email notifications
                </label>
                <label className="row" style={{ alignItems: 'center' }}>
                    <input
                        type="checkbox"
                        checked={pushEnabled}
                        onChange={(event) => updatePushPreference(event.target.checked)}
                        style={{ width: 'auto' }}
                    />
                    Push notifications
                </label>
            </section>
        </div>
    );
}
