// CareerPulse - Dashboard Analytics Charts (Chart.js)
function initDashboardCharts() {
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js library is not yet loaded.');
        return;
    }

    // 1. Application Status Doughnut Chart
    const statusCanvas = document.getElementById('statusChart');
    const statusEmpty = document.getElementById('statusEmptyState');

    if (statusCanvas && window.statusData) {
        const statusValues = Object.values(window.statusData).map(v => Number(v) || 0);
        const totalStatus = statusValues.reduce((sum, val) => sum + val, 0);

        if (totalStatus === 0) {
            statusCanvas.style.display = 'none';
            if (statusEmpty) statusEmpty.classList.remove('d-none');
        } else {
            statusCanvas.style.display = 'block';
            if (statusEmpty) statusEmpty.classList.add('d-none');

            // Color palette mapped to status names
            const colorMap = {
                'Applied': '#6366F1',     // Indigo
                'In Progress': '#0EA5E9', // Sky Blue
                'Interviews': '#F59E0B',  // Amber
                'Offers': '#10B981',      // Emerald Green
                'Rejected': '#EF4444'     // Rose Red
            };

            const labels = Object.keys(window.statusData);
            const backgroundColors = labels.map(label => colorMap[label] || '#8B5CF6');

            new Chart(statusCanvas, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: statusValues,
                        backgroundColor: backgroundColors,
                        borderWidth: 2,
                        borderColor: '#FFFFFF',
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                boxWidth: 12,
                                padding: 14,
                                font: {
                                    family: 'Plus Jakarta Sans',
                                    size: 12,
                                    weight: '600'
                                },
                                generateLabels: function (chart) {
                                    const data = chart.data;
                                    if (data.labels.length && data.datasets.length) {
                                        return data.labels.map((label, i) => {
                                            const value = data.datasets[0].data[i] || 0;
                                            return {
                                                text: `${label} (${value})`,
                                                fillStyle: data.datasets[0].backgroundColor[i],
                                                strokeStyle: '#FFFFFF',
                                                lineWidth: 1,
                                                hidden: isNaN(data.datasets[0].data[i]) || chart.getDatasetMeta(0).data[i].hidden,
                                                index: i
                                            };
                                        });
                                    }
                                    return [];
                                }
                            }
                        },
                        tooltip: {
                            backgroundColor: '#1E1B4B',
                            titleFont: { family: 'Plus Jakarta Sans', weight: 'bold', size: 13 },
                            bodyFont: { family: 'Plus Jakarta Sans', size: 12 },
                            padding: 10,
                            cornerRadius: 8,
                            callbacks: {
                                label: function (context) {
                                    const count = context.parsed || 0;
                                    const pct = totalStatus > 0 ? ((count / totalStatus) * 100).toFixed(1) : 0;
                                    return ` ${context.label}: ${count} (${pct}%)`;
                                }
                            }
                        }
                    },
                    cutout: '68%'
                }
            });
        }
    }

    // 2. Monthly Application Activity Bar Chart
    const trendCanvas = document.getElementById('trendChart');
    const trendEmpty = document.getElementById('trendEmptyState');

    if (trendCanvas && window.trendData) {
        const trendValues = Object.values(window.trendData).map(v => Number(v) || 0);
        const totalTrend = trendValues.reduce((sum, val) => sum + val, 0);

        if (totalTrend === 0) {
            trendCanvas.style.display = 'none';
            if (trendEmpty) trendEmpty.classList.remove('d-none');
        } else {
            trendCanvas.style.display = 'block';
            if (trendEmpty) trendEmpty.classList.add('d-none');

            new Chart(trendCanvas, {
                type: 'bar',
                data: {
                    labels: Object.keys(window.trendData),
                    datasets: [{
                        label: 'Applications Submitted',
                        data: trendValues,
                        backgroundColor: 'rgba(124, 58, 237, 0.85)',
                        hoverBackgroundColor: '#6D28D9',
                        borderRadius: 8,
                        barThickness: 'flex',
                        maxBarThickness: 32
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: '#1E1B4B',
                            titleFont: { family: 'Plus Jakarta Sans', weight: 'bold', size: 13 },
                            bodyFont: { family: 'Plus Jakarta Sans', size: 12 },
                            padding: 10,
                            cornerRadius: 8,
                            callbacks: {
                                label: function (context) {
                                    return ` Submitted: ${context.parsed.y} application${context.parsed.y === 1 ? '' : 's'}`;
                                }
                            }
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1,
                                precision: 0,
                                font: { family: 'Plus Jakarta Sans', size: 11, weight: '500' },
                                color: '#6B7280'
                            },
                            grid: {
                                color: '#F3F4F6'
                            }
                        },
                        x: {
                            grid: { display: false },
                            ticks: {
                                font: { family: 'Plus Jakarta Sans', size: 12, weight: '600' },
                                color: '#4B5563'
                            }
                        }
                    }
                }
            });
        }
    }
}

// Execute upon DOM readiness
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDashboardCharts);
} else {
    initDashboardCharts();
}
