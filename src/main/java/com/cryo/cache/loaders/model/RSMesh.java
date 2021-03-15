package com.cryo.cache.loaders.model;

import com.cryo.cache.io.InputStream;
import com.cryo.cache.loaders.TextureDefinitions;
import com.cryo.cache.loaders.animations.AnimationDefinitions;
import com.cryo.utils.Utilities;

import java.util.Arrays;

public class RSMesh {

    public int id;
    public int version = 12;
    public int vertexCount;
    public int maxDepth;
    public int faceCount;
    public byte priority;
    public int texturedFaceCount;
    public int[] textureRenderTypes;
    public int[] vertexX;
    public int[] vertexY;
    public int[] vertexZ;
    public short[] triangleX;
    public short[] triangleY;
    public short[] triangleZ;
    public int[] vertexSkins;
    public byte[] faceType;
    public byte[] facePriorities;
    public byte[] faceAlphas;
    public int[] textureSkins;
    public short[] faceTextures;
    public short[] faceColor;
    public int[] realFaceColour;
    public byte[] texturePos;
    public short[] texTriX;
    public short[] texTriY;
    public short[] texTriZ;
    public int[] particleDirectionX;
    public int[] particleDirectionY;
    public int[] particleDirectionZ;
    public int[] particleLifespanX;
    public int[] particleLifespanY;
    public int[] particleLifespanZ;
    public int[] texturePrimaryColor;
    public int[] textureSecondaryColor;
    public short[] aShortArray1980;
    public short[] aShortArray1981;
    public AnimationDefinitions render;
    public AnimationDefinitions animation;

    public TextureDefinitions[] textureDefinitions;

    public int[][] animationBones;

    private ParticleEmitterConfig[] particleConfig;
    private SurfaceSkin[] surfaceSkins;
    private VertexNormal[] isolatedVertexNormals;

    public RSMesh() {
        vertexCount = 0;
        faceCount = 0;
        texturedFaceCount = 0;
        int i_3 = 0;
        int i_4 = 0;
        int i_5 = 0;
        boolean bool_6 = false;
        boolean bool_7 = false;
        boolean bool_8 = false;
        boolean bool_9 = false;
        boolean bool_10 = false;
        boolean bool_11 = false;
        priority = -1;
    }

    public void decode(byte[] data) {
        if (data[data.length - 1] == -1 && data[data.length - 2] == -1) {
            decodeNewFormat(data);
        } else {
            decodeOldFormat(data);
        }
    }

    public void decodeNewFormat(byte[] data) {
        InputStream first = new InputStream(data);
        InputStream second = new InputStream(data);
        InputStream third = new InputStream(data);
        InputStream fourth = new InputStream(data);
        InputStream fifth = new InputStream(data);
        InputStream sixth = new InputStream(data);
        InputStream seventh = new InputStream(data);
        first.setOffset(data.length - 23);
        vertexCount = first.readUnsignedShort();
        faceCount = first.readUnsignedShort();
        texturedFaceCount = first.readUnsignedByte();
        int i_9 = first.readUnsignedByte();
        boolean hasFaceRenderTypes = (i_9 & 0x1) == 1;
        boolean hasParticleEffects = (i_9 & 0x2) == 2;
        boolean hasBillboards = (i_9 & 0x4) == 4;
        boolean hasVersion = (i_9 & 0x8) == 8;
        if (hasVersion) {
            first.setOffset(first.getOffset() - 7);
            version = first.readUnsignedByte();
            first.setOffset(first.getOffset() + 6);
        }

        int modelPriority = first.readUnsignedByte();
        int hasFaceAlpha = first.readUnsignedByte();
        int hasFaceSkins = first.readUnsignedByte();
        int hasFaceTextures = first.readUnsignedByte();
        int hasVertexSkins = first.readUnsignedByte();
        int modelVerticesX = first.readUnsignedShort();
        int modelVerticesY = first.readUnsignedShort();
        int modelVerticesZ = first.readUnsignedShort();
        int faceIndices = first.readUnsignedShort();
        int textureIndices = first.readUnsignedShort();
        int numVertexSkins = 0;
        int i_25 = 0;
        int i_26 = 0;
        if (texturedFaceCount > 0) {
            textureRenderTypes = new int[texturedFaceCount];
            first.setOffset(0);

            for (int i = 0; i < texturedFaceCount; i++) {
                int b_28 = textureRenderTypes[i] = first.readByte();
                if (b_28 == 0) {
                    ++numVertexSkins;
                }

                if (b_28 >= 1 && b_28 <= 3) {
                    ++i_25;
                }

                if (b_28 == 2) {
                    ++i_26;
                }
            }
        }

        int totalFaces = texturedFaceCount;
        int flagBufferOffset = totalFaces;
        totalFaces += vertexCount;
        int i_29 = totalFaces;
        if (hasFaceRenderTypes) {
            totalFaces += faceCount;
        }

        int i_30 = totalFaces;
        totalFaces += faceCount;
        int i_31 = totalFaces;
        if (modelPriority == 255) {
            totalFaces += faceCount;
        }

        int i_32 = totalFaces;
        if (hasFaceSkins == 1) {
            totalFaces += faceCount;
        }

        int vertSkinsBufferOffset = totalFaces;
        if (hasVertexSkins == 1) {
            totalFaces += vertexCount;
        }

        int i_34 = totalFaces;
        if (hasFaceAlpha == 1) {
            totalFaces += faceCount;
        }

        int i_35 = totalFaces;
        totalFaces += faceIndices;
        int i_36 = totalFaces;
        if (hasFaceTextures == 1) {
            totalFaces += faceCount * 2;
        }

        int i_37 = totalFaces;
        totalFaces += textureIndices;
        int i_38 = totalFaces;
        totalFaces += faceCount * 2;
        int vertXBufferOffset = totalFaces;
        totalFaces += modelVerticesX;
        int vertYBufferOffset = totalFaces;
        totalFaces += modelVerticesY;
        int vertZBufferOffset = totalFaces;
        totalFaces += modelVerticesZ;
        int simple_tex_pmn_offset = totalFaces;
        totalFaces += numVertexSkins * 6;
        int i_43 = totalFaces;
        totalFaces += i_25 * 6;
        byte b_44 = 6;
        if (version == 14) {
            b_44 = 7;
        } else if (version >= 15) {
            b_44 = 9;
        }

        int i_45 = totalFaces;
        totalFaces += i_25 * b_44;
        int i_46 = totalFaces;
        totalFaces += i_25;
        int i_47 = totalFaces;
        totalFaces += i_25;
        int i_48 = totalFaces;
        totalFaces = i_26 * 2 + totalFaces + i_25;
        vertexX = new int[vertexCount];
        vertexY = new int[vertexCount];
        vertexZ = new int[vertexCount];
        triangleX = new short[faceCount];
        triangleY = new short[faceCount];
        triangleZ = new short[faceCount];
        if (hasVertexSkins == 1) {
            vertexSkins = new int[vertexCount];
        }

        if (hasFaceRenderTypes) {
            faceType = new byte[faceCount];
        }

        if (modelPriority == 255) {
            facePriorities = new byte[faceCount];
        } else {
            priority = (byte) modelPriority;
        }

        if (hasFaceAlpha == 1) {
            faceAlphas = new byte[faceCount];
        }

        if (hasFaceSkins == 1) {
            textureSkins = new int[faceCount];
        }

        if (hasFaceTextures == 1) {
            faceTextures = new short[faceCount];
        }

        if (hasFaceTextures == 1 && texturedFaceCount > 0) {
            texturePos = new byte[faceCount];
        }

        faceColor = new short[faceCount];
        if (texturedFaceCount > 0) {
            texTriX = new short[texturedFaceCount];
            texTriY = new short[texturedFaceCount];
            texTriZ = new short[texturedFaceCount];
            if (i_25 > 0) {
                particleDirectionX = new int[i_25];
                particleDirectionY = new int[i_25];
                particleDirectionZ = new int[i_25];
                particleLifespanX = new int[i_25];
                particleLifespanY = new int[i_25];
                particleLifespanZ = new int[i_25];
            }

            if (i_26 > 0) {
                texturePrimaryColor = new int[i_26];
                textureSecondaryColor = new int[i_26];
            }
        }


        first.setOffset(flagBufferOffset);
        second.setOffset(vertXBufferOffset);
        third.setOffset(vertYBufferOffset);
        fourth.setOffset(vertZBufferOffset);
        fifth.setOffset(vertSkinsBufferOffset);
        int baseX = 0;
        int baseY = 0;
        int baseZ = 0;

        for (int vertex = 0; vertex < vertexCount; vertex++) {
            int offsetFlags = first.readUnsignedByte();
            int vertextOffsetX = 0;
            if ((offsetFlags & 0x1) != 0) {
                vertextOffsetX = second.readUnsignedSmart2();
            }

            int vertextOffsetY = 0;
            if ((offsetFlags & 0x2) != 0) {
                vertextOffsetY = third.readUnsignedSmart2();
            }

            int vertetxOffsetZ = 0;
            if ((offsetFlags & 0x4) != 0) {
                vertetxOffsetZ = fourth.readUnsignedSmart2();
            }

            vertexX[vertex] = baseX + vertextOffsetX;
            vertexY[vertex] = baseY + vertextOffsetY;
            vertexZ[vertex] = baseZ + vertetxOffsetZ;
            baseX = vertexX[vertex];
            baseY = vertexY[vertex];
            baseZ = vertexZ[vertex];
            if (hasVertexSkins == 1) {
                vertexSkins[vertex] = fifth.readUnsignedByte();
            }
        }

        first.setOffset(i_38);
        second.setOffset(i_29);
        third.setOffset(i_31);
        fourth.setOffset(i_34);
        fifth.setOffset(i_32);
        sixth.setOffset(i_36);
        seventh.setOffset(i_37);

        for (int i_53 = 0; i_53 < faceCount; i_53++) {
            faceColor[i_53] = (short) first.readUnsignedShort();
            if (hasFaceRenderTypes) {
                faceType[i_53] = (byte) second.readByte();
            }

            if (modelPriority == 255) {
                facePriorities[i_53] = (byte) third.readByte();
            }

            if (hasFaceAlpha == 1) {
                faceAlphas[i_53] = (byte) fourth.readByte();
            }

            if (hasFaceSkins == 1) {
                textureSkins[i_53] = fifth.readUnsignedByte();
            }

            if (hasFaceTextures == 1) {
                faceTextures[i_53] = (short) (sixth.readUnsignedShort() - 1);
            }

            if (texturePos != null) {
                if (faceTextures[i_53] != -1) {
                    texturePos[i_53] = (byte) (seventh.readUnsignedByte() - 1);
                } else {
                    texturePos[i_53] = -1;
                }
            }
        }

        maxDepth = -1;
        first.setOffset(i_35);
        second.setOffset(i_30);
        calculateMaxDepth(first, second);
        first.setOffset(simple_tex_pmn_offset);
        second.setOffset(i_43);
        third.setOffset(i_45);
        fourth.setOffset(i_46);
        fifth.setOffset(i_47);
        sixth.setOffset(i_48);
        decodeTexturedTriangles(first, second, third, fourth, fifth, sixth);
        first.setOffset(totalFaces);
        if (hasParticleEffects) {
            int emitterCount = first.readUnsignedByte();
            if (emitterCount > 0) {
                particleConfig = new ParticleEmitterConfig[emitterCount];

                for (int i = 0; i < emitterCount; i++) {
                    int particleId = first.readUnsignedShort();
                    int faceIdx = first.readUnsignedShort();
                    byte priority;
                    if (modelPriority == 255) {
                        priority = facePriorities[faceIdx];
                    } else {
                        priority = (byte) modelPriority;
                    }

                    particleConfig[i] = new ParticleEmitterConfig(particleId, triangleX[faceIdx], triangleY[faceIdx], triangleZ[faceIdx], priority);
                }
            }

            int surfaceSkinCount = first.readUnsignedByte();
            if (surfaceSkinCount > 0) {
                surfaceSkins = new SurfaceSkin[surfaceSkinCount];

                for (int i = 0; i < surfaceSkinCount; i++) {
                    int x = first.readUnsignedShort();
                    int y = first.readUnsignedShort();
                    surfaceSkins[i] = new SurfaceSkin(x, y);
                }
            }
        }

        if (hasBillboards) {
            int i_53 = first.readUnsignedByte();
            if (i_53 > 0) {
                isolatedVertexNormals = new VertexNormal[i_53];

                for (int i = 0; i < i_53; i++) {
                    int vertextOffsetX = first.readUnsignedShort();
                    int vertextOffsetY = first.readUnsignedShort();
                    int vertetxOffsetZ = first.readUnsignedByte();
                    byte b_58 = (byte) first.readByte();
                    isolatedVertexNormals[i] = new VertexNormal(vertextOffsetX, vertextOffsetY, vertetxOffsetZ, b_58);
                }
            }
        }
    }

    public void decodeOldFormat(byte[] data) {
        boolean bool_2 = false;
        boolean bool_3 = false;
        InputStream first = new InputStream(data);
        InputStream second = new InputStream(data);
        InputStream third = new InputStream(data);
        InputStream fourth = new InputStream(data);
        InputStream firth = new InputStream(data);
        first.setOffset(data.length - 18);
        vertexCount = first.readUnsignedShort();
        faceCount = first.readUnsignedShort();
        texturedFaceCount = first.readUnsignedByte();
        int i_9 = first.readUnsignedByte();
        int i_10 = first.readUnsignedByte();
        int i_11 = first.readUnsignedByte();
        int i_12 = first.readUnsignedByte();
        int i_13 = first.readUnsignedByte();
        int i_14 = first.readUnsignedShort();
        int i_15 = first.readUnsignedShort();
        int i_16 = first.readUnsignedShort();
        int i_17 = first.readUnsignedShort();
        byte b_18 = 0;
        int i_42 = b_18 + vertexCount;
        int i_20 = i_42;
        i_42 += faceCount;
        int i_21 = i_42;
        if (i_10 == 255) {
            i_42 += faceCount;
        }

        int i_22 = i_42;
        if (i_12 == 1) {
            i_42 += faceCount;
        }

        int i_23 = i_42;
        if (i_9 == 1) {
            i_42 += faceCount;
        }

        int i_24 = i_42;
        if (i_13 == 1) {
            i_42 += vertexCount;
        }

        int i_25 = i_42;
        if (i_11 == 1) {
            i_42 += faceCount;
        }

        int i_26 = i_42;
        i_42 += i_17;
        int i_27 = i_42;
        i_42 += faceCount * 2;
        int i_28 = i_42;
        i_42 += texturedFaceCount * 6;
        int i_29 = i_42;
        i_42 += i_14;
        int i_30 = i_42;
        i_42 += i_15;
        int i_10000 = i_42 + i_16;
        vertexX = new int[vertexCount];
        vertexY = new int[vertexCount];
        vertexZ = new int[vertexCount];
        triangleX = new short[faceCount];
        triangleY = new short[faceCount];
        triangleZ = new short[faceCount];
        if (texturedFaceCount > 0) {
            textureRenderTypes = new int[texturedFaceCount];
            texTriX = new short[texturedFaceCount];
            texTriY = new short[texturedFaceCount];
            texTriZ = new short[texturedFaceCount];
        }

        if (i_13 == 1) {
            vertexSkins = new int[vertexCount];
        }

        if (i_9 == 1) {
            faceType = new byte[faceCount];
            texturePos = new byte[faceCount];
            faceTextures = new short[faceCount];
        }

        if (i_10 == 255) {
            facePriorities = new byte[faceCount];
        } else {
            priority = (byte) i_10;
        }

        if (i_11 == 1) {
            faceAlphas = new byte[faceCount];
        }

        if (i_12 == 1) {
            textureSkins = new int[faceCount];
        }

        faceColor = new short[faceCount];
        first.setOffset(b_18);
        second.setOffset(i_29);
        third.setOffset(i_30);
        fourth.setOffset(i_42);
        firth.setOffset(i_24);
        int i_32 = 0;
        int i_33 = 0;
        int i_34 = 0;

        int i_35;
        int i_36;
        int i_39;
        for (i_35 = 0; i_35 < vertexCount; i_35++) {
            i_36 = first.readUnsignedByte();
            int i_37 = 0;
            if ((i_36 & 0x1) != 0) {
                i_37 = second.readUnsignedSmart2();
            }

            int i_38 = 0;
            if ((i_36 & 0x2) != 0) {
                i_38 = third.readUnsignedSmart2();
            }

            i_39 = 0;
            if ((i_36 & 0x4) != 0) {
                i_39 = fourth.readUnsignedSmart2();
            }

            vertexX[i_35] = i_32 + i_37;
            vertexY[i_35] = i_33 + i_38;
            vertexZ[i_35] = i_34 + i_39;
            i_32 = vertexX[i_35];
            i_33 = vertexY[i_35];
            i_34 = vertexZ[i_35];
            if (i_13 == 1) {
                vertexSkins[i_35] = firth.readUnsignedByte();
            }
        }

        first.setOffset(i_27);
        second.setOffset(i_23);
        third.setOffset(i_21);
        fourth.setOffset(i_25);
        firth.setOffset(i_22);

        for (i_35 = 0; i_35 < faceCount; i_35++) {
            faceColor[i_35] = (short) first.readUnsignedShort();
            if (i_9 == 1) {
                i_36 = second.readUnsignedByte();
                if ((i_36 & 0x1) == 1) {
                    faceType[i_35] = 1;
                    bool_2 = true;
                } else {
                    faceType[i_35] = 0;
                }

                if ((i_36 & 0x2) == 2) {
                    texturePos[i_35] = (byte) (i_36 >> 2);
                    faceTextures[i_35] = faceColor[i_35];
                    faceColor[i_35] = 127;
                    if (faceTextures[i_35] != -1) {
                        bool_3 = true;
                    }
                } else {
                    texturePos[i_35] = -1;
                    faceTextures[i_35] = -1;
                }
            }

            if (i_10 == 255) {
                facePriorities[i_35] = (byte) third.readByte();
            }

            if (i_11 == 1) {
                faceAlphas[i_35] = (byte) fourth.readByte();
            }

            if (i_12 == 1) {
                textureSkins[i_35] = firth.readUnsignedByte();
            }
        }

        maxDepth = -1;
        first.setOffset(i_26);
        second.setOffset(i_20);
        short s_43 = 0;
        short s_44 = 0;
        short s_45 = 0;
        short s_46 = 0;

        int i_40;
        for (i_39 = 0; i_39 < faceCount; i_39++) {
            i_40 = second.readUnsignedByte();
            if (i_40 == 1) {
                s_43 = (short) (first.readUnsignedSmart2() + s_46);
                s_44 = (short) (first.readUnsignedSmart2() + s_43);
                s_45 = (short) (first.readUnsignedSmart2() + s_44);
                s_46 = s_45;
                triangleX[i_39] = s_43;
                triangleY[i_39] = s_44;
                triangleZ[i_39] = s_45;
                if (s_43 > maxDepth) {
                    maxDepth = s_43;
                }

                if (s_44 > maxDepth) {
                    maxDepth = s_44;
                }

                if (s_45 > maxDepth) {
                    maxDepth = s_45;
                }
            }

            if (i_40 == 2) {
                s_44 = s_45;
                s_45 = (short) (first.readUnsignedSmart2() + s_46);
                s_46 = s_45;
                triangleX[i_39] = s_43;
                triangleY[i_39] = s_44;
                triangleZ[i_39] = s_45;
                if (s_45 > maxDepth) {
                    maxDepth = s_45;
                }
            }

            if (i_40 == 3) {
                s_43 = s_45;
                s_45 = (short) (first.readUnsignedSmart2() + s_46);
                s_46 = s_45;
                triangleX[i_39] = s_43;
                triangleY[i_39] = s_44;
                triangleZ[i_39] = s_45;
                if (s_45 > maxDepth) {
                    maxDepth = s_45;
                }
            }

            if (i_40 == 4) {
                short s_41 = s_43;
                s_43 = s_44;
                s_44 = s_41;
                s_45 = (short) (first.readUnsignedSmart2() + s_46);
                s_46 = s_45;
                triangleX[i_39] = s_43;
                triangleY[i_39] = s_41;
                triangleZ[i_39] = s_45;
                if (s_45 > maxDepth) {
                    maxDepth = s_45;
                }
            }
        }

        ++maxDepth;
        first.setOffset(i_28);

        for (i_39 = 0; i_39 < texturedFaceCount; i_39++) {
            textureRenderTypes[i_39] = 0;
            texTriX[i_39] = (short) first.readUnsignedShort();
            texTriY[i_39] = (short) first.readUnsignedShort();
            texTriZ[i_39] = (short) first.readUnsignedShort();
        }

        if (texturePos != null) {
            boolean bool_47 = false;

            for (i_40 = 0; i_40 < faceCount; i_40++) {
                int i_48 = texturePos[i_40] & 0xff;
                if (i_48 != 255) {
                    if (triangleX[i_40] == (texTriX[i_48] & 0xffff) && triangleY[i_40] == (texTriY[i_48] & 0xffff) && triangleZ[i_40] == (texTriZ[i_48] & 0xffff)) {
                        texturePos[i_40] = -1;
                    } else {
                        bool_47 = true;
                    }
                }
            }

            if (!bool_47) {
                texturePos = null;
            }
        }

        if (!bool_3) {
            faceTextures = null;
        }

        if (!bool_2) {
            faceType = null;
        }
    }

    public void setRealColours() {
        short[] colours = this.faceColor;
        int[] realColours = new int[colours.length];
        for(int i = 0; i < colours.length; i++) {
            int colour = Utilities.hslToRgb(colours[i] & 0xffff);
            realColours[i] = colour;
            int r = (colour>>16)&0xFF;
            int g = (colour>>8)&0xFF;
            int b = colour&0xFF;
        }
        this.realFaceColour = realColours;
    }

    public int[][] getBones(boolean bool_1) {
        int[] ints_2 = new int[256];
        int i_3 = 0;
        int i_4 = bool_1 ? vertexCount : maxDepth;

        int i_6;
        for (int i_5 = 0; i_5 < i_4; i_5++) {
            i_6 = vertexSkins[i_5];
            if (i_6 >= 0) {
                ++ints_2[i_6];
                if (i_6 > i_3) {
                    i_3 = i_6;
                }
            }
        }

        int[][] ints_8 = new int[i_3 + 1][];

        for (i_6 = 0; i_6 <= i_3; i_6++) {
            ints_8[i_6] = new int[ints_2[i_6]];
            ints_2[i_6] = 0;
        }

        for (i_6 = 0; i_6 < i_4; i_6++) {
            int i_7 = vertexSkins[i_6];
            if (i_7 >= 0) {
                ints_8[i_7][ints_2[i_7]++] = i_6;
            }
        }

        return ints_8;
    }

    void calculateMaxDepth(InputStream first, InputStream second) {
        short s_3 = 0;
        short s_4 = 0;
        short s_5 = 0;
        short s_6 = 0;

        for (int i_7 = 0; i_7 < faceCount; i_7++) {
            int i_8 = second.readUnsignedByte();
            if (i_8 == 1) {
                s_3 = (short) (first.readUnsignedSmart2() + s_6);
                s_4 = (short) (first.readUnsignedSmart2() + s_3);
                s_5 = (short) (first.readUnsignedSmart2() + s_4);
                s_6 = s_5;
                triangleX[i_7] = s_3;
                triangleY[i_7] = s_4;
                triangleZ[i_7] = s_5;
                if (s_3 > maxDepth) {
                    maxDepth = s_3;
                }

                if (s_4 > maxDepth) {
                    maxDepth = s_4;
                }

                if (s_5 > maxDepth) {
                    maxDepth = s_5;
                }
            }

            if (i_8 == 2) {
                s_4 = s_5;
                s_5 = (short) (first.readUnsignedSmart2() + s_6);
                s_6 = s_5;
                triangleX[i_7] = s_3;
                triangleY[i_7] = s_4;
                triangleZ[i_7] = s_5;
                if (s_5 > maxDepth) {
                    maxDepth = s_5;
                }
            }

            if (i_8 == 3) {
                s_3 = s_5;
                s_5 = (short) (first.readUnsignedSmart2() + s_6);
                s_6 = s_5;
                triangleX[i_7] = s_3;
                triangleY[i_7] = s_4;
                triangleZ[i_7] = s_5;
                if (s_5 > maxDepth) {
                    maxDepth = s_5;
                }
            }

            if (i_8 == 4) {
                short s_9 = s_3;
                s_3 = s_4;
                s_4 = s_9;
                s_5 = (short) (first.readUnsignedSmart2() + s_6);
                s_6 = s_5;
                triangleX[i_7] = s_3;
                triangleY[i_7] = s_9;
                triangleZ[i_7] = s_5;
                if (s_5 > maxDepth) {
                    maxDepth = s_5;
                }
            }
        }

        ++maxDepth;
    }

    void decodeTexturedTriangles(InputStream first, InputStream second, InputStream third, InputStream fourth, InputStream fifth, InputStream sixth) {
        for (int face = 0; face < texturedFaceCount; face++) {
            int type = textureRenderTypes[face] & 0xff;
            if (type == 0) {
                texTriX[face] = (short) first.readUnsignedShort();
                texTriY[face] = (short) first.readUnsignedShort();
                texTriZ[face] = (short) first.readUnsignedShort();
            }

            if (type == 1) {
                texTriX[face] = (short) second.readUnsignedShort();
                texTriY[face] = (short) second.readUnsignedShort();
                texTriZ[face] = (short) second.readUnsignedShort();
                if (version < 15) {
                    particleDirectionX[face] = third.readUnsignedShort();
                    if (version < 14) {
                        particleDirectionY[face] = third.readUnsignedShort();
                    } else {
                        particleDirectionY[face] = third.read24BitUnsignedInt();
                    }

                    particleDirectionZ[face] = third.readUnsignedShort();
                } else {
                    particleDirectionX[face] = third.read24BitUnsignedInt();
                    particleDirectionY[face] = third.read24BitUnsignedInt();
                    particleDirectionZ[face] = third.read24BitUnsignedInt();
                }

                particleLifespanX[face] = fourth.readByte();
                particleLifespanY[face] = fifth.readByte();
                particleLifespanZ[face] = sixth.readByte();
            }

            if (type == 2) {
                texTriX[face] = (short) second.readUnsignedShort();
                texTriY[face] = (short) second.readUnsignedShort();
                texTriZ[face] = (short) second.readUnsignedShort();
                if (version < 15) {
                    particleDirectionX[face] = third.readUnsignedShort();
                    if (version < 14) {
                        particleDirectionY[face] = third.readUnsignedShort();
                    } else {
                        particleDirectionY[face] = third.read24BitUnsignedInt();
                    }

                    particleDirectionZ[face] = third.readUnsignedShort();
                } else {
                    particleDirectionX[face] = third.read24BitUnsignedInt();
                    particleDirectionY[face] = third.read24BitUnsignedInt();
                    particleDirectionZ[face] = third.read24BitUnsignedInt();
                }

                particleLifespanX[face] = fourth.readByte();
                particleLifespanY[face] = fifth.readByte();
                particleLifespanZ[face] = sixth.readByte();
                texturePrimaryColor[face] = sixth.readByte();
                textureSecondaryColor[face] = sixth.readByte();
            }

            if (type == 3) {
                texTriX[face] = (short) second.readUnsignedShort();
                texTriY[face] = (short) second.readUnsignedShort();
                texTriZ[face] = (short) second.readUnsignedShort();
                if (version < 15) {
                    particleDirectionX[face] = third.readUnsignedShort();
                    if (version < 14) {
                        particleDirectionY[face] = third.readUnsignedShort();
                    } else {
                        particleDirectionY[face] = third.read24BitUnsignedInt();
                    }

                    particleDirectionZ[face] = third.readUnsignedShort();
                } else {
                    particleDirectionX[face] = third.read24BitUnsignedInt();
                    particleDirectionY[face] = third.read24BitUnsignedInt();
                    particleDirectionZ[face] = third.read24BitUnsignedInt();
                }

                particleLifespanX[face] = fourth.readByte();
                particleLifespanY[face] = fifth.readByte();
                particleLifespanZ[face] = sixth.readByte();
            }
        }

    }

    public RSMesh(RSMesh[] meshes, int size) {
        vertexCount = 0;
        faceCount = 0;
        texturedFaceCount = 0;
        int i_3 = 0;
        int i_4 = 0;
        int i_5 = 0;
        boolean faceTypes = false;
        boolean bool_7 = false;
        boolean bool_8 = false;
        boolean bool_9 = false;
        boolean bool_10 = false;
        boolean bool_11 = false;
        priority = -1;

        int index;
        for (index = 0; index < size; index++) {
            RSMesh mesh = meshes[index];
            if (mesh != null) {
                vertexCount += mesh.vertexCount;
                faceCount += mesh.faceCount;
                texturedFaceCount += mesh.texturedFaceCount;
                if (mesh.particleConfig != null) {
                    i_3 += mesh.particleConfig.length;
                }

                if (mesh.surfaceSkins != null) {
                    i_4 += mesh.surfaceSkins.length;
                }

                if (mesh.isolatedVertexNormals != null) {
                    i_5 += mesh.isolatedVertexNormals.length;
                }

                faceTypes |= mesh.faceType != null;
                if (mesh.facePriorities != null) {
                    bool_7 = true;
                } else {
                    if (priority == -1) {
                        priority = mesh.priority;
                    }

                    if (priority != mesh.priority) {
                        bool_7 = true;
                    }
                }

                bool_8 |= mesh.faceAlphas != null;
                bool_9 |= mesh.texturePos != null;
                bool_10 |= mesh.faceTextures != null;
                bool_11 |= mesh.textureSkins != null;
            }
        }

        vertexX = new int[vertexCount];
        vertexY = new int[vertexCount];
        vertexZ = new int[vertexCount];
        vertexSkins = new int[vertexCount];
        aShortArray1980 = new short[vertexCount];
        triangleX = new short[faceCount];
        triangleY = new short[faceCount];
        triangleZ = new short[faceCount];
        if (faceTypes) {
            faceType = new byte[faceCount];
        }

        if (bool_7) {
            facePriorities = new byte[faceCount];
        }

        if (bool_8) {
            faceAlphas = new byte[faceCount];
        }

        if (bool_9) {
            texturePos = new byte[faceCount];
        }

        faceColor = new short[faceCount];
        if (bool_10) {
            faceTextures = new short[faceCount];
        }

        if (bool_11) {
            textureSkins = new int[faceCount];
        }

        aShortArray1981 = new short[faceCount];
        if (texturedFaceCount > 0) {
            textureRenderTypes = new int[texturedFaceCount];
            texTriX = new short[texturedFaceCount];
            texTriY = new short[texturedFaceCount];
            texTriZ = new short[texturedFaceCount];
            particleDirectionX = new int[texturedFaceCount];
            particleDirectionY = new int[texturedFaceCount];
            particleDirectionZ = new int[texturedFaceCount];
            particleLifespanX = new int[texturedFaceCount];
            particleLifespanY = new int[texturedFaceCount];
            particleLifespanZ = new int[texturedFaceCount];
            texturePrimaryColor = new int[texturedFaceCount];
            textureSecondaryColor = new int[texturedFaceCount];
        }

        if (i_5 > 0) {
            isolatedVertexNormals = new VertexNormal[i_5];
        }

        if (i_3 > 0) {
            particleConfig = new ParticleEmitterConfig[i_3];
        }

        if (i_4 > 0) {
            surfaceSkins = new SurfaceSkin[i_4];
        }

        vertexCount = 0;
        faceCount = 0;
        texturedFaceCount = 0;
        i_3 = 0;
        i_4 = 0;
        i_5 = 0;

        int i_16;
        for (index = 0; index < size; index++) {
            short s_13 = (short) (1 << index);
            RSMesh rsmesh_14 = meshes[index];
            if (rsmesh_14 != null) {
                int i_15;
                if (rsmesh_14.isolatedVertexNormals != null) {
                    for (i_15 = 0; i_15 < rsmesh_14.isolatedVertexNormals.length; i_15++) {
                        VertexNormal normals = rsmesh_14.isolatedVertexNormals[i_15];
                        isolatedVertexNormals[i_5++] = normals.method1459(normals.anInt809 + faceCount);
                    }
                }

                for (i_15 = 0; i_15 < rsmesh_14.faceCount; i_15++) {
                    if (faceTypes && rsmesh_14.faceType != null) {
                        faceType[faceCount] = rsmesh_14.faceType[i_15];
                    }

                    if (bool_7) {
                        if (rsmesh_14.facePriorities != null) {
                            facePriorities[faceCount] = rsmesh_14.facePriorities[i_15];
                        } else {
                            facePriorities[faceCount] = rsmesh_14.priority;
                        }
                    }

                    if (bool_8 && rsmesh_14.faceAlphas != null) {
                        faceAlphas[faceCount] = rsmesh_14.faceAlphas[i_15];
                    }

                    if (bool_10) {
                        if (rsmesh_14.faceTextures != null) {
                            faceTextures[faceCount] = rsmesh_14.faceTextures[i_15];
                        } else {
                            faceTextures[faceCount] = -1;
                        }
                    }

                    if (bool_11) {
                        if (rsmesh_14.textureSkins != null) {
                            textureSkins[faceCount] = rsmesh_14.textureSkins[i_15];
                        } else {
                            textureSkins[faceCount] = -1;
                        }
                    }

                    triangleX[faceCount] = (short) method2657(rsmesh_14, rsmesh_14.triangleX[i_15], s_13);
                    triangleY[faceCount] = (short) method2657(rsmesh_14, rsmesh_14.triangleY[i_15], s_13);
                    triangleZ[faceCount] = (short) method2657(rsmesh_14, rsmesh_14.triangleZ[i_15], s_13);
                    aShortArray1981[faceCount] = s_13;
                    faceColor[faceCount] = rsmesh_14.faceColor[i_15];
                    ++faceCount;
                }

                if (rsmesh_14.particleConfig != null) {
                    for (i_15 = 0; i_15 < rsmesh_14.particleConfig.length; i_15++) {
                        i_16 = method2657(rsmesh_14, rsmesh_14.particleConfig[i_15].faceX, s_13);
                        int i_17 = method2657(rsmesh_14, rsmesh_14.particleConfig[i_15].faceY, s_13);
                        int i_18 = method2657(rsmesh_14, rsmesh_14.particleConfig[i_15].faceZ, s_13);
                        particleConfig[i_3] = rsmesh_14.particleConfig[i_15].method1488(i_16, i_17, i_18);
                        ++i_3;
                    }
                }

                if (rsmesh_14.surfaceSkins != null) {
                    for (i_15 = 0; i_15 < rsmesh_14.surfaceSkins.length; i_15++) {
                        i_16 = method2657(rsmesh_14, rsmesh_14.surfaceSkins[i_15].anInt2119, s_13);
                        surfaceSkins[i_4] = rsmesh_14.surfaceSkins[i_15].method2911(i_16);
                        ++i_4;
                    }
                }
            }
        }

        index = 0;
        maxDepth = vertexCount;

        for (int i_23 = 0; i_23 < size; i_23++) {
            short s_19 = (short) (1 << i_23);
            RSMesh rsmesh_20 = meshes[i_23];
            if (rsmesh_20 != null) {
                for (i_16 = 0; i_16 < rsmesh_20.faceCount; i_16++) {
                    if (bool_9) {
                        texturePos[index++] = (byte) (rsmesh_20.texturePos != null && rsmesh_20.texturePos[i_16] != -1 ? texturedFaceCount + rsmesh_20.texturePos[i_16] : -1);
                    }
                }

                for (i_16 = 0; i_16 < rsmesh_20.texturedFaceCount; i_16++) {
                    int b_24 = textureRenderTypes[texturedFaceCount] = rsmesh_20.textureRenderTypes[i_16];
                    if (b_24 == 0) {
                        texTriX[texturedFaceCount] = (short) method2657(rsmesh_20, rsmesh_20.texTriX[i_16], s_19);
                        texTriY[texturedFaceCount] = (short) method2657(rsmesh_20, rsmesh_20.texTriY[i_16], s_19);
                        texTriZ[texturedFaceCount] = (short) method2657(rsmesh_20, rsmesh_20.texTriZ[i_16], s_19);
                    }

                    if (b_24 >= 1 && b_24 <= 3) {
                        texTriX[texturedFaceCount] = rsmesh_20.texTriX[i_16];
                        texTriY[texturedFaceCount] = rsmesh_20.texTriY[i_16];
                        texTriZ[texturedFaceCount] = rsmesh_20.texTriZ[i_16];
                        particleDirectionX[texturedFaceCount] = rsmesh_20.particleDirectionX[i_16];
                        particleDirectionY[texturedFaceCount] = rsmesh_20.particleDirectionY[i_16];
                        particleDirectionZ[texturedFaceCount] = rsmesh_20.particleDirectionZ[i_16];
                        particleLifespanX[texturedFaceCount] = rsmesh_20.particleLifespanX[i_16];
                        particleLifespanY[texturedFaceCount] = rsmesh_20.particleLifespanY[i_16];
                        particleLifespanZ[texturedFaceCount] = rsmesh_20.particleLifespanZ[i_16];
                    }

                    if (b_24 == 2) {
                        texturePrimaryColor[texturedFaceCount] = rsmesh_20.texturePrimaryColor[i_16];
                        textureSecondaryColor[texturedFaceCount] = rsmesh_20.textureSecondaryColor[i_16];
                    }

                    ++texturedFaceCount;
                }
            }
        }

    }

    public void recolour(short s_1, short s_2) {
        for (int i_3 = 0; i_3 < faceCount; i_3++) {
            if (faceColor[i_3] == s_1) {
                faceColor[i_3] = s_2;
            }
        }

    }

    public void retexture(short s_1, short s_2) {
        if (faceTextures != null) {
            for (int i_3 = 0; i_3 < faceCount; i_3++) {
                if (faceTextures[i_3] == s_1) {
                    faceTextures[i_3] = s_2;
                }
            }
        }

    }

    int method2657(RSMesh rsmesh_1, int i_2, short s_3) {
        int i_4 = rsmesh_1.vertexX[i_2];
        int i_5 = rsmesh_1.vertexY[i_2];
        int i_6 = rsmesh_1.vertexZ[i_2];

        for (int i_7 = 0; i_7 < vertexCount; i_7++) {
            if (i_4 == vertexX[i_7] && i_5 == vertexY[i_7] && i_6 == vertexZ[i_7]) {
                aShortArray1980[i_7] |= s_3;
                return i_7;
            }
        }

        vertexX[vertexCount] = i_4;
        vertexY[vertexCount] = i_5;
        vertexZ[vertexCount] = i_6;
        aShortArray1980[vertexCount] = s_3;
        vertexSkins[vertexCount] = rsmesh_1.vertexSkins != null ? rsmesh_1.vertexSkins[i_2] : -1;
        return vertexCount++;
    }

    public void upscale() {
        int i_2;
        for (i_2 = 0; i_2 < vertexCount; i_2++) {
            vertexX[i_2] <<= 2;
            vertexY[i_2] <<= 2;
            vertexZ[i_2] <<= 2;
        }

        if (texturedFaceCount > 0 && particleDirectionX != null) {
            for (i_2 = 0; i_2 < particleDirectionX.length; i_2++) {
                particleDirectionX[i_2] <<= 2;
                particleDirectionY[i_2] <<= 2;
                if (textureRenderTypes[i_2] != 1) {
                    particleDirectionZ[i_2] <<= 2;
                }
            }
        }

    }
}