Parse.Cloud.beforeSave('ChatRoom', async (request) => {
    const chatRoom = request.object;
    const participants = chatRoom.get('participants');

    if (participants && participants.length > 0) {
        const acl = new Parse.ACL();
        participants.forEach((participant) => {
            acl.setReadAccess(participant.id, true);
            acl.setWriteAccess(participant.id, true);
        });

        chatRoom.setACL(acl);
        console.log('ChatRoom ACL set for participants:', participants.map(p => p.id));
    } else {
        const user = request.user;
        if (user) {
            const acl = new Parse.ACL(user);
            chatRoom.setACL(acl);
            console.log('ChatRoom ACL set for creator:', user.id);
        } else {
            console.log('No participants and no creator found for ChatRoom');
        }
    }
});

Parse.Cloud.beforeSave('Message', async (request) => {
    const message = request.object;
    const chatRoom = message.get('chatRoom');

    if (chatRoom) {
        console.log(`Fetching ChatRoom for message: ${message.id}`);
        await chatRoom.fetch({ useMasterKey: true });

        const acl = chatRoom.getACL();
        if (acl) {
            message.setACL(acl);
            console.log('Message ACL set from ChatRoom:', chatRoom.id);
        } else {
            console.log('ChatRoom ACL is null');
        }
    } else {
        console.log('No chatRoom found in Message');
    }
});