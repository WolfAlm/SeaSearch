<template>
    <label style="vertical-align:middle; font-size: 70px" class="title_grad">SeaSearch</label>
    <div class="text" style="margin: 0 auto;">
        <p class="text__text">
            Note that we use your Telegram account for authentication purposes only. By registering, you automatically
            give us permission to view your messages, thus allowing the app to analyze them. All requested data is
            strictly necessary for the service to work. Your messages are not stored anywhere, being processed in
            batches of 100 without any persistence.
        </p>
        <p class="text__text">
            In this app you can access:
        </p>
        <ul class="text__text">
            <li class="li"> 1. A word-cloud of your most popular words</li>
            <li class="li">2. A plot demonstrating the frequency of your messages day-to-day
            </li>
            <li class="li">3. A table describing incoming and outgoing message statistics
            </li>
        </ul>
        <p class="text__text">
            <span class="text__span">IMPORTANT!</span> Telegram allows us to process only 100 messages per second, so
            the app may work for a long time. You can leave it running and come back the next day.
        </p>
    </div>
</template>
<script>
export default {
    name: 'InfoText'
}
</script>

<style>
@import '../assets/styles/info.css';
</style>
