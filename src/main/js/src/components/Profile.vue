<template>
    <body>
    <div class="centered box">
        <div>
            <img style="float:left; width: 5vw; margin: 2.5% 2%" src="../assets/logo.ico" alt="SeaSearch Logo"/>
            <span style="float:left; vertical-align:middle" class="profile_title_grad noselect">SeaSearch</span>
            <button style="margin: 3% 2%; width: 8vw" class="button" @click="back">All chats</button>
            <button style="margin: 3% 1%; width: 8vw" class="button" @click="loadData">Refresh</button>
        </div>
        <hr>
        <div class="row">
            <div style="flex: 30%;">
                <img
                    style="border: solid black 5px;width: 10vw; border-radius: 50%; margin-top: 2vw; margin-bottom: 1vw"
                    :src="this.img" alt="Chat logo"/>
                <h2 style="font-size: 30px">{{ title }}</h2>
                <h2 style="font-size: 20px"> {{ isPrivate }}</h2>
                <table v-if="!loading" class="table" style="max-height: 15vw; margin-bottom: 1.5vw">
                    <tbody>
                    <tr>
                        <td>Avg. messages <br> per day:</td>
                        <td>{{ stats.misc.meanMessages }}</td>
                    </tr>
                    <tr>
                        <td>Max messages <br> per day:</td>
                        <td>{{ stats.misc.maxMessages }}</td>
                    </tr>
                    <tr>
                        <td>Last message:</td>
                        <td>{{ stats.misc.dateFirstMessage }}</td>
                    </tr>
                    <tr>
                        <td>Active days:</td>
                        <td>{{ stats.misc.daysActive }}</td>
                    </tr>
                    </tbody>
                </table>
                <button v-if="!loading" style=" float:none;width: 20vw; margin: 0.5vw" class="button"
                        @click="showChart">{{ buttonText1 }}
                </button>
                <button v-if="!loading" style=" float:none;width: 20vw; margin: 0.5vw" class="button"
                        @click="showWordCloud">{{ buttonText2 }}
                </button>
            </div>
            <div v-if="loading" style="flex:60%;">
                <h2 style="margin-top: 5vw;font-size: 30px; width: 40vw">Loading data. This may take a while, get
                    yourself a cup of coffee</h2>
            </div>
            <div v-if="!loading && showState === 0" style="flex:60%;margin-top: 4vw;">
                <h2 style="font-size: 35px; ">Chat statistics:</h2>
                <table class="table" style="height: 35vw">
                    <thead>
                    <tr>
                        <th></th>
                        <th>Total</th>
                        <th>Incoming</th>
                        <th>Outgoing</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="item in tableData" :key="item[0]">
                        <td v-for="(col, index) in item" :key="index">{{ col }}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div v-show="!loading && showState === 1" style="flex:60%;margin-top: 2vw">
                <h2 style="font-size: 35px; ">Word frequency chart:</h2>
                <canvas id="chart" class="table" style="height: 37vw; border: solid #033083 3px">
                </canvas>
            </div>
            <div v-show="!loading && showState === 2" style="flex:60%;margin-top: 2vw;">
                <h2 style="font-size: 35px; ">Top 100 Word cloud:</h2>
                <canvas id="wordCloud" class="table" style="height: 37vw; width: 37vw; border: solid #033083 3px">
                </canvas>
            </div>
        </div>
    </div>
    </body>
</template>

<script>


import {Chart, LinearScale, TimeScale} from "chart.js";
import UserService from "../services/user.service";
import {WordCloudController, WordElement} from 'chartjs-chart-wordcloud';
import moment from 'moment'
import 'chartjs-adapter-moment';

export default {

    name: "ProfileStats",
    data() {
        return {
            id: this.$route.params.id,
            imgUrl: this.$route.query.img,
            title: this.$route.query.title,
            isPrivate: this.$route.query.isPrivate,
            img: "",
            loading: true,
            showState: 0,
            stats: {
                incoming: {
                    messages: 0, words: 0, symbols: 0, stickers: 0, photos: 0, videos: 0, audio: 0, documents: 0
                },
                outgoing: {
                    messages: 0, words: 0, symbols: 0, stickers: 0, photos: 0, videos: 0, audio: 0, documents: 0
                },
                misc: {
                    meanMessages: 0, daysActive: 0, maxMessages: 0, dateFirstMessage: "",
                }
            },
            ctxCloud: null,
            ctxChart: null,
            words: [],
            days: [],
            buttonText1: "Frequency chart",
            buttonText2: "Word cloud",
            wordCloudInit: false,
            chartInit: false
        }
    },
    mounted() {
        if (this.imgUrl) {
            UserService.getImage(this.imgUrl).then(res => {
                const reader = new FileReader()
                reader.onloadend = () => {
                    this.img = reader.result;
                    this.isPrivate = this.$route.query.isPrivate;
                }
                reader.readAsDataURL(res.data)
            })
        } else {
            this.img = "data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg'  viewBox='0 0 48 48' width='96px' height='96px'><path fill='%2329b6f6' d='M24 4A20 20 0 1 0 24 44A20 20 0 1 0 24 4Z'/><path fill='%23fff' d='M33.95,15l-3.746,19.126c0,0-0.161,0.874-1.245,0.874c-0.576,0-0.873-0.274-0.873-0.274l-8.114-6.733 l-3.97-2.001l-5.095-1.355c0,0-0.907-0.262-0.907-1.012c0-0.625,0.933-0.923,0.933-0.923l21.316-8.468 c-0.001-0.001,0.651-0.235,1.126-0.234C33.667,14,34,14.125,34,14.5C34,14.75,33.95,15,33.95,15z'/><path fill='%23b0bec5' d='M23,30.505l-3.426,3.374c0,0-0.149,0.115-0.348,0.12c-0.069,0.002-0.143-0.009-0.219-0.043 l0.964-5.965L23,30.505z'/><path fill='%23cfd8dc' d='M29.897,18.196c-0.169-0.22-0.481-0.26-0.701-0.093L16,26c0,0,2.106,5.892,2.427,6.912 c0.322,1.021,0.58,1.045,0.58,1.045l0.964-5.965l9.832-9.096C30.023,18.729,30.064,18.416,29.897,18.196z'/></svg>"
        }
        this.loadData()
        this.ctxCloud = document.getElementById('wordCloud').getContext('2d');
        this.ctxChart = document.getElementById('chart').getContext('2d');
    },
    computed: {
        tableData() {
            let res = []
            for (const [key, value] of Object.entries(this.stats.incoming)) {
                res.push([this.capitalize(key), this.stats.outgoing[key] + value, value, this.stats.outgoing[key]]);
            }
            return res;
        }
    },
    methods: {
        capitalize(string) {
            return string.charAt(0).toUpperCase() + string.slice(1);
        },
        back() {
            this.$router.push("/profiles")

        },
        loadData() {
            this.loading = true
            UserService.getStats(`/chats/${this.id}/info`).then(
                res => {
                    ({
                        incomingMessage: this.stats.incoming.messages,
                        incomingWords: this.stats.incoming.words,
                        incomingSymbols: this.stats.incoming.symbols,
                        incomingAudio: this.stats.incoming.audio,
                        incomingSticker: this.stats.incoming.stickers,
                        incomingPhoto: this.stats.incoming.photos,
                        incomingVideo: this.stats.incoming.videos,
                        incomingDocument: this.stats.incoming.documents,
                        outgoingMessage: this.stats.outgoing.messages,
                        outgoingWord: this.stats.outgoing.words,
                        outgoingSymbols: this.stats.outgoing.symbols,
                        outgoingAudio: this.stats.outgoing.audio,
                        outgoingSticker: this.stats.outgoing.stickers,
                        outgoingPhoto: this.stats.outgoing.photos,
                        outgoingVideo: this.stats.outgoing.videos,
                        outgoingDocument: this.stats.outgoing.documents,
                        countAverageMessage: this.stats.misc.meanMessages,
                        dateFirstMessage: this.stats.misc.dateFirstMessage,
                        countDaysActive: this.stats.misc.daysActive,
                        countMaxMessage: this.stats.misc.maxMessages
                    } = res.data);
                    this.stats.misc.dateFirstMessage = moment(res.dateFirstMessage).format('YYYY/MM/DD hh:mm')
                    this.loading = false
                    console.log("Loading done")
                }
            )
            UserService.getStats(`/chats/${this.id}/wordcloud`).then(
                res => {
                    this.words = res.data;
                    this.drawWordCloud()
                }
            )
            UserService.getStats(`/chats/${this.id}/graph`).then(
                res => {
                    this.days = res.data;
                    this.drawChart()
                }
            )
        },
        drawChart() {
            const dataOutgoing = this.days.outgoingDailyMessages.map(it => {
                return {'x': moment(`${it.date[0]}-${it.date[1]}-${it.date[2]}`, 'DD-MM-YYYY'), 'y': it.count}
            }).sort()
            const dataIncoming = this.days.incomingDailyMessages.map(it => {
                return {'x': moment(`${it.date[0]}-${it.date[1]}-${it.date[2]}`, 'DD-MM-YYYY'), 'y': it.count}
            }).sort()
            console.log(dataIncoming)
            Chart.register(TimeScale)
            const chart = new Chart(this.ctxChart, {
                type: 'line',
                data: dataIncoming,
                options: {
                    responsive: false,
                    scales: {
                        x: {
                            type: 'time',
                            time: {
                                unit: 'month'
                            }
                        }
                    }
                }
            });
        },
        showChart() {
            if (this.showState === 1) {
                this.buttonText1 = "Frequency chart";
                this.buttonText2 = "Word cloud";
                this.showState = 0;
                return
            }
            this.buttonText1 = "General stats";
            this.buttonText2 = "Word cloud";
            this.showState = 1;
        },
        drawWordCloud() {
            const data = {
                labels: this.words.map((d) => d.word),
                datasets: [
                    {
                        label: '',
                        data: this.words.map((d) => 10 + d.count * 10),
                    },
                ],
            };
            Chart.register(LinearScale, WordCloudController, WordElement)
            const myChart = new Chart(this.ctxCloud, {
                type: WordCloudController.id,
                data: data,
                options: {
                    title: {
                        display: false,
                        text: 'Top 100 WordCloud',
                    },
                    responsive: false,
                    plugins: {
                        legend: {
                            display: false,
                        },
                    },
                },
            });

        },
        showWordCloud() {
            if (this.showState === 2) {
                this.buttonText1 = "Frequency chart";
                this.buttonText2 = "Word cloud";
                this.showState = 0;
                return
            }
            this.buttonText1 = "Frequency chart";
            this.buttonText2 = "General stats";
            this.showState = 2
        }
    }
}
</script>

<style scoped>
@import '../assets/styles/base.css';
@import "../assets/styles/profile.css";
</style>